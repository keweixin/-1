package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bylw.config.RecommendConfigHolder;
import com.bylw.dto.FoodDTO;
import com.bylw.dto.RecommendResultDTO;
import com.bylw.entity.*;
import com.bylw.mapper.*;
import com.bylw.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合推荐服务：协同过滤 + 特征匹配
 * 论文公式：
 *   公式2-1（余弦相似度）: sim(u,v) = Σ(i∈I∩v) ru,i·rv,i / (√Σi∈u ru,i² × √Σi∈v rv,i²)
 *   公式2-2（加权融合）:    Score(u,j) = α·CF(u,j) + β·FM(u,j), α+β=1
 *   时间衰减: w(t) = w₀ × exp(-λ·Δt), Δt=距今天数, λ=0.1（半衰期约7天）
 */
@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private FoodMapper foodMapper;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private RecipeMapper recipeMapper;
    @Autowired
    private UserBehaviorMapper behaviorMapper;
    @Autowired
    private FoodProfileMapper foodProfileMapper;
    @Autowired
    private FoodTagRelationMapper foodTagRelationMapper;
    @Autowired
    private FoodTagMapper foodTagMapper;
    @Autowired
    private FoodCategoryMapper categoryMapper;
    @Autowired
    private RecommendConfigHolder configHolder;

    @Override
    public RecommendResultDTO getRecommendFoods(Integer userId, int limit) {
        RecommendResultDTO result = new RecommendResultDTO();

        // 1. 获取候选食品
        LambdaQueryWrapper<Food> foodWrapper = new LambdaQueryWrapper<>();
        foodWrapper.eq(Food::getStatus, 1).eq(Food::getDeleted, 0);
        List<Food> candidates = foodMapper.selectList(foodWrapper);
        int candidateCount = candidates.size();
        result.setCandidateCount(candidateCount);

        if (candidates.isEmpty()) {
            result.setFoods(List.of());
            result.setHealthFilteredCount(0);
            result.setTasteMatchedCount(0);
            result.setFinalCount(0);
            result.setColdStart(true);
            return result;
        }

        // 2. 饮食档案健康过滤
        FoodProfile profile = null;
        if (userId != null) {
            profile = getUserFoodProfile(userId);
            candidates = healthFilter(candidates, profile);
        }
        int healthFilteredCount = candidates.size();
        result.setHealthFilteredCount(healthFilteredCount);

        if (candidates.isEmpty()) {
            result.setFoods(List.of());
            result.setTasteMatchedCount(0);
            result.setFinalCount(0);
            result.setColdStart(true);
            return result;
        }

        // 3. 无行为数据的新用户 → 基于标签匹配推荐（冷启动）
        Map<Integer, BigDecimal> cfScores = new HashMap<>();
        Map<Integer, Double>   fmScores = new HashMap<>();
        boolean isColdStart = (userId == null || !hasBehaviorData(userId));
        result.setColdStart(isColdStart);

        if (isColdStart) {
            // 冷启动：仅用特征匹配
            fmScores = computeFeatureMatching(candidates, profile, null);
        } else {
            // 3. 协同过滤评分
            cfScores = computeCollaborativeFiltering(userId, candidates);
            // 4. 特征匹配评分
            fmScores = computeFeatureMatching(candidates, profile, userId);
        }

        // 5. 加权融合排序
        final Map<Integer, BigDecimal> cfFinal = cfScores;
        final Map<Integer, Double>     fmFinal = fmScores;
        final double alphaUsed = isColdStart ? 0.0 : configHolder.getAlpha();
        final double betaUsed  = isColdStart ? 1.0 : configHolder.getBeta();

        // Count how many got positive feature matching
        int tasteMatchedCount = (int) candidates.stream()
            .filter(f -> fmFinal.getOrDefault(f.getFoodId(), 0.0) > 0.1)
            .count();
        result.setTasteMatchedCount(tasteMatchedCount);

        List<Map.Entry<Food, Double>> scored = candidates.stream()
            .map(food -> {
                BigDecimal cf = cfFinal.getOrDefault(food.getFoodId(), BigDecimal.ZERO);
                Double fm = fmFinal.getOrDefault(food.getFoodId(), 0.0);
                double score = alphaUsed * cf.doubleValue() + betaUsed * fm;
                return new AbstractMap.SimpleEntry<>(food, score);
            })
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(limit)
            .collect(Collectors.toList());

        result.setFinalCount(scored.size());

        List<Food> resultFoods = scored.stream().map(Map.Entry::getKey).collect(Collectors.toList());

        List<Integer> categoryIds = resultFoods.stream()
            .map(Food::getCategoryId).filter(Objects::nonNull).distinct().toList();

        Map<Integer, String> categoryNameMap = categoryIds.isEmpty() ? Map.of() :
            categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(FoodCategory::getCategoryId, FoodCategory::getCategoryName));

        final Map<Integer, String> finalCategoryNameMap = categoryNameMap;
        final FoodProfile finalProfile = profile;

        result.setFoods(scored.stream()
            .map(e -> toFoodDTO(e.getKey(), finalCategoryNameMap, e.getValue(),
                cfFinal, fmFinal, finalProfile))
            .toList());
        return result;
    }

    private FoodDTO toFoodDTO(Food food,
                               Map<Integer, String> categoryNameMap,
                               Double matchScore,
                               Map<Integer, BigDecimal> cfScores,
                               Map<Integer, Double> fmScores,
                               FoodProfile profile) {
        FoodDTO dto = new FoodDTO();
        dto.setFoodId(food.getFoodId());
        dto.setMerchantId(food.getMerchantId());
        dto.setCategoryId(food.getCategoryId());
        dto.setFoodName(food.getFoodName());
        dto.setOriginPrice(food.getOriginPrice());
        dto.setDiscountPrice(food.getDiscountPrice());
        dto.setStockQty(food.getStockQty());
        dto.setExpireDate(food.getExpireDate());
        dto.setNutritionDesc(food.getNutritionDesc());
        dto.setTasteDesc(food.getTasteDesc());
        dto.setSuitablePeople(food.getSuitablePeople());
        dto.setAllergenInfo(food.getAllergenInfo());
        dto.setCoverImg(food.getCoverImg());
        dto.setDescription(food.getDescription());
        dto.setStatus(food.getStatus());
        dto.setCreateTime(food.getCreateTime());
        dto.setCategoryName(categoryNameMap.get(food.getCategoryId()));
        dto.setMatchScore(matchScore);

        // Individual score breakdown
        double cf = cfScores.getOrDefault(food.getFoodId(), BigDecimal.ZERO).doubleValue();
        double fm = fmScores.getOrDefault(food.getFoodId(), 0.0);
        dto.setCfScore(Math.round(cf * 100.0) / 100.0);
        dto.setFeatureScore(Math.round(fm * 100.0) / 100.0);

        // Build match reason and matched tags
        List<String> reasons = new ArrayList<>();
        List<String> tags = new ArrayList<>();

        if (cf > 0.1) {
            reasons.add("相似用户偏好");
        }
        if (profile != null && profile.getTastePreference() != null) {
            Set<String> userTastes = parseSet(profile.getTastePreference());
            if (food.getTasteDesc() != null) {
                for (String ut : userTastes) {
                    if (food.getTasteDesc().contains(ut)) {
                        tags.add(ut);
                    }
                }
            }
            if (!tags.isEmpty()) {
                reasons.add("口味匹配: " + String.join("、", tags));
            }
        }
        if (fm > 0.3 && food.getSuitablePeople() != null && !food.getSuitablePeople().isBlank()) {
            reasons.add("适宜人群匹配");
            tags.add(food.getSuitablePeople());
        }

        dto.setMatchReason(reasons.isEmpty() ? "综合推荐" : String.join("；", reasons));
        dto.setMatchedTags(tags);
        return dto;
    }

    /**
     * 协同过滤（CF）
     * 公式2-1：sim(u,v) = Σ(i∈I∩v) ru,i·rv,i / (√Σi∈u ru,i² × √Σi∈v rv,i²)
     * 对每个候选食品 j：
     *   CF(u,j) = Σ(v≠u) sim(u,v) × r(v,j) / Σ(v≠u) |sim(u,v)|
     */
    private Map<Integer, BigDecimal> computeCollaborativeFiltering(Integer userId, List<Food> candidates) {
        Set<Integer> candidateIds = candidates.stream()
            .map(Food::getFoodId).collect(Collectors.toSet());

        // 提取目标用户行为向量 r(u,·)
        Map<Integer, BigDecimal> targetVector = buildUserVector(userId, candidateIds);

        // 收集所有其他用户（近30天有行为）及其行为向量
        List<? extends Map.Entry<Integer, Map<Integer, BigDecimal>>> otherUsers = getRecentUserVectors(candidateIds);
        otherUsers = otherUsers.stream()
            .filter(e -> !e.getKey().equals(userId))
            .toList();

        // 计算目标用户向量模长（公式分母一部分）
        double targetNorm = vectorNorm(targetVector);

        Map<Integer, BigDecimal> cfScores = new HashMap<>();

        for (Food food : candidates) {
            Integer foodId = food.getFoodId();
            double weightedSum = 0.0;
            double simSum = 0.0;

            for (Map.Entry<Integer, Map<Integer, BigDecimal>> entry : otherUsers) {
                Map<Integer, BigDecimal> otherVector = entry.getValue();
                BigDecimal rating = otherVector.get(foodId); // r(v, j)

                if (rating == null) continue;

                double otherNorm = vectorNorm(otherVector);
                if (otherNorm == 0.0 || targetNorm == 0.0) continue;

                // 余弦相似度：分子 = Σ ru,i·rv,i
                double dotProduct = dotProduct(targetVector, otherVector);
                double sim = dotProduct / (targetNorm * otherNorm); // ∈ [-1, 1]

                if (sim > 0) { // 只取正相关
                    weightedSum += sim * rating.doubleValue();
                    simSum += sim;
                }
            }

            double cfScore = simSum > 0 ? weightedSum / simSum : 0.0;
            // 归一化到 [0, 1]
            cfScores.put(foodId, BigDecimal.valueOf(Math.min(cfScore / 5.0, 1.0)));
        }

        return cfScores;
    }

    /**
     * 特征匹配（FM）
     * 匹配维度：口味偏好(tasteDesc) vs 用户taste_preference
     *           营养标签(food_tag) vs 用户taste_preference
     *           适宜人群(suitable_people) vs 慢病史
     */
    private Map<Integer, Double> computeFeatureMatching(List<Food> candidates, FoodProfile profile, Integer userId) {
        Map<Integer, Double> fmScores = new HashMap<>();

        // 构建用户口味偏好集合（支持模糊匹配：每个词独立匹配）
        Set<String> userTastes = new HashSet<>();
        if (profile != null && profile.getTastePreference() != null) {
            for (String t : profile.getTastePreference().split("[,，、\\s]+")) {
                String trimmed = t.trim();
                if (!trimmed.isEmpty()) {
                    userTastes.add(trimmed);
                    // 同时添加单字/关键词用于模糊匹配
                    if (trimmed.length() > 1) {
                        userTastes.add(trimmed.substring(0, Math.min(2, trimmed.length())));
                    }
                }
            }
        }

        // 构建用户慢病史集合
        Set<String> userDiseases = new HashSet<>();
        if (profile != null && profile.getChronicDisease() != null) {
            for (String d : profile.getChronicDisease().split("[,，、\\s]+")) {
                String trimmed = d.trim();
                if (!trimmed.isEmpty()) {
                    userDiseases.add(trimmed);
                }
            }
        }

        // 预取所有食品的标签集合
        Map<Integer, Set<String>> foodTagMap = buildFoodTagMap(
            candidates.stream().map(Food::getFoodId).toList()
        );

        for (Food food : candidates) {
            double score = 0.0;

            // ① 口味字段模糊匹配（tasteDesc 匹配用户口味偏好）
            if (food.getTasteDesc() != null && !food.getTasteDesc().isBlank() && !userTastes.isEmpty()) {
                String foodTaste = food.getTasteDesc();
                for (String ut : userTastes) {
                    if (foodTaste.contains(ut)) {
                        score += 0.4;
                        break;
                    }
                }
            }

            // ② 标签匹配（食品标签匹配用户口味偏好）
            Set<String> tags = foodTagMap.getOrDefault(food.getFoodId(), Set.of());
            for (String tag : tags) {
                if (userTastes.contains(tag)) {
                    score += 0.2;
                }
            }

            // ③ 适宜人群匹配（慢病史用户优先看到适合其状况的食品）
            if (profile != null && profile.getChronicDisease() != null && food.getSuitablePeople() != null) {
                String suitable = food.getSuitablePeople();
                if ((userDiseases.contains("高血糖") || userDiseases.contains("糖尿病"))
                        && (suitable.contains("高血糖") || suitable.contains("糖尿病") || suitable.contains("低糖") || suitable.contains("无糖"))) {
                    score += 0.3;
                }
                if (userDiseases.contains("高血压") && (suitable.contains("高血压") || suitable.contains("低盐") || suitable.contains("清淡"))) {
                    score += 0.3;
                }
                if ((userDiseases.contains("健身") || userDiseases.contains("减脂"))
                        && (suitable.contains("健身") || suitable.contains("减脂") || suitable.contains("高蛋白") || suitable.contains("低脂"))) {
                    score += 0.3;
                }
                if (userDiseases.contains("老年人") && suitable.contains("老人")) {
                    score += 0.2;
                }
                if (userDiseases.contains("儿童") && suitable.contains("儿童")) {
                    score += 0.2;
                }
                if (userDiseases.contains("孕妇") && suitable.contains("孕妇")) {
                    score += 0.2;
                }
            }

            // 归一化到 [0, 1]
            double normalized = Math.min(score, 1.0);
            fmScores.put(food.getFoodId(), normalized);
        }

        return fmScores;
    }

    /**
     * 饮食档案健康过滤
     * 过滤规则：
     *   - allergen_info 包含过敏原 → 排除（"无" 除外）
     *   - 慢病史用户忌口 → 排除（辣→胃病）
     *   - 用户 tabooInfo → 排除
     */
    private List<Food> healthFilter(List<Food> candidates, FoodProfile profile) {
        if (profile == null) return candidates;

        Set<String> allergens = parseSet(profile.getAllergenInfo()).stream()
            .filter(a -> !a.equals("无") && !a.equals("暂无") && !a.equals("无过敏原"))
            .collect(Collectors.toSet());
        Set<String> taboos = parseSet(profile.getTabooInfo()).stream()
            .filter(t -> !t.equals("无") && !t.equals("暂无"))
            .collect(Collectors.toSet());
        Set<String> disease = parseSet(profile.getChronicDisease()).stream()
            .filter(d -> !d.equals("无") && !d.equals("暂无"))
            .collect(Collectors.toSet());

        return candidates.stream().filter(food -> {
            if (!allergens.isEmpty() && food.getAllergenInfo() != null
                    && !food.getAllergenInfo().trim().equals("无")) {
                String foodAllergen = food.getAllergenInfo();
                for (String a : allergens) {
                    if (foodAllergen.contains(a)) return false;
                }
            }
            if (!taboos.isEmpty() && food.getTasteDesc() != null) {
                String foodTaste = food.getTasteDesc();
                for (String t : taboos) {
                    if (foodTaste.contains(t)) return false;
                }
            }
            if (!disease.isEmpty()) {
                if (food.getTasteDesc() != null) {
                    String foodTaste = food.getTasteDesc();
                    if (disease.contains("胃病") && (foodTaste.contains("辣") || foodTaste.contains("麻辣"))) {
                        return false;
                    }
                    if ((disease.contains("高血糖") || disease.contains("糖尿病")) && foodTaste.contains("高糖")) {
                        return false;
                    }
                }
            }
            return true;
        }).toList();
    }

    /** 从 user_behavior 表构建用户行为向量（带时间衰减） */
    private Map<Integer, BigDecimal> buildUserVector(Integer userId, Set<Integer> candidateIds) {
        Map<Integer, BigDecimal> vector = new HashMap<>();
        int windowDays = configHolder.getBehaviorWindowDays();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(windowDays);

        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehavior::getUserId, userId)
               .eq(UserBehavior::getTargetType, "food")
               .ge(UserBehavior::getCreateTime, cutoff);
        List<UserBehavior> behaviors = behaviorMapper.selectList(wrapper);

        for (UserBehavior b : behaviors) {
            if (!candidateIds.contains(b.getTargetId())) continue;
            double lambda = configHolder.getLambda();
            double days = ChronoUnit.HOURS.between(b.getCreateTime(), LocalDateTime.now()) / 24.0;
            double decay = Math.exp(-lambda * days);
            double score = b.getWeightScore().doubleValue() * decay;
            vector.merge(b.getTargetId(), BigDecimal.valueOf(score), (a, b2) -> a.add(b2));
        }
        return vector;
    }

    /** 获取所有其他用户近期的行为向量 */
    private List<? extends Map.Entry<Integer, Map<Integer, BigDecimal>>> getRecentUserVectors(Set<Integer> candidateIds) {
        int windowDays = configHolder.getBehaviorWindowDays();
        LocalDateTime cutoff = LocalDateTime.now().minusDays(windowDays);
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(UserBehavior::getCreateTime, cutoff)
               .eq(UserBehavior::getTargetType, "food");
        List<UserBehavior> allBehaviors = behaviorMapper.selectList(wrapper);

        double lambda = configHolder.getLambda();

        Map<Integer, List<UserBehavior>> byUser = allBehaviors.stream()
            .collect(Collectors.groupingBy(UserBehavior::getUserId));

        return byUser.entrySet().stream()
            .map(entry -> {
                Map<Integer, BigDecimal> vec = new HashMap<>();
                for (UserBehavior b : entry.getValue()) {
                    if (!candidateIds.contains(b.getTargetId())) continue;
                    double days = ChronoUnit.HOURS.between(b.getCreateTime(), LocalDateTime.now()) / 24.0;
                    double decay = Math.exp(-lambda * days);
                    double score = b.getWeightScore().doubleValue() * decay;
                    vec.merge(b.getTargetId(), BigDecimal.valueOf(score), (a, b2) -> a.add(b2));
                }
                return new AbstractMap.SimpleEntry<>(entry.getKey(), vec);
            })
            .filter(e -> !e.getValue().isEmpty())
            .toList();
    }

    /** 向量点积 Σ a_i · b_i */
    private double dotProduct(Map<Integer, BigDecimal> a, Map<Integer, BigDecimal> b) {
        double sum = 0.0;
        for (Map.Entry<Integer, BigDecimal> e : a.entrySet()) {
            BigDecimal bv = b.get(e.getKey());
            if (bv != null) sum += e.getValue().doubleValue() * bv.doubleValue();
        }
        return sum;
    }

    /** 向量模长 √Σ a_i² */
    private double vectorNorm(Map<Integer, BigDecimal> vec) {
        double sum = 0.0;
        for (BigDecimal v : vec.values()) {
            sum += v.doubleValue() * v.doubleValue();
        }
        return Math.sqrt(sum);
    }

    /** 预构建食品ID→标签名集合的映射 */
    private Map<Integer, Set<String>> buildFoodTagMap(List<Integer> foodIds) {
        if (foodIds.isEmpty()) return Map.of();

        LambdaQueryWrapper<FoodTagRelation> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.in(FoodTagRelation::getFoodId, foodIds);
        List<FoodTagRelation> relations = foodTagRelationMapper.selectList(relWrapper);

        Set<Integer> tagIds = relations.stream()
            .map(FoodTagRelation::getTagId).collect(Collectors.toSet());

        Map<Integer, String> tagIdToName = new HashMap<>();
        if (!tagIds.isEmpty()) {
            List<FoodTag> tags = foodTagMapper.selectBatchIds(tagIds);
            for (FoodTag tag : tags) {
                tagIdToName.put(tag.getTagId(), tag.getTagName());
            }
        }

        Map<Integer, Set<String>> result = new HashMap<>();
        for (FoodTagRelation r : relations) {
            String tagName = tagIdToName.get(r.getTagId());
            if (tagName != null) {
                result.computeIfAbsent(r.getFoodId(), k -> new HashSet<>()).add(tagName);
            }
        }
        return result;
    }

    private FoodProfile getUserFoodProfile(Integer userId) {
        LambdaQueryWrapper<FoodProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodProfile::getUserId, userId);
        return foodProfileMapper.selectOne(wrapper);
    }

    private boolean hasBehaviorData(Integer userId) {
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehavior::getUserId, userId)
               .eq(UserBehavior::getTargetType, "food")
               .last("LIMIT 1");
        return behaviorMapper.selectCount(wrapper) > 0;
    }

    private Set<String> parseSet(String s) {
        if (s == null || s.isBlank()) return Set.of();
        return Arrays.stream(s.split("[,，]"))
            .map(String::trim)
            .filter(t -> !t.isEmpty())
            .collect(Collectors.toSet());
    }

    @Override
    public List<Article> getRecommendArticles(Integer userId, int limit) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1)
               .eq(Article::getDeleted, 0)
               .orderByDesc(Article::getReadCount)
               .last("LIMIT " + limit);
        return articleMapper.selectList(wrapper);
    }

    @Override
    public List<Recipe> getRecommendRecipes(Integer userId, int limit) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recipe::getStatus, 1)
               .eq(Recipe::getDeleted, 0)
               .orderByDesc(Recipe::getCollectCount)
               .last("LIMIT " + limit);
        return recipeMapper.selectList(wrapper);
    }

    @Override
    public void recordBehavior(Integer userId, String targetType, Integer targetId, String behaviorType) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (targetType == null || targetType.isBlank()) {
            throw new IllegalArgumentException("目标类型不能为空");
        }
        if (targetId == null || targetId <= 0) {
            throw new IllegalArgumentException("目标ID无效");
        }
        if (behaviorType == null || behaviorType.isBlank()) {
            throw new IllegalArgumentException("行为类型不能为空");
        }
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setTargetType(targetType);
        behavior.setTargetId(targetId);
        behavior.setBehaviorType(behaviorType);
        behavior.setWeightScore(getWeight(behaviorType));
        behavior.setCreateTime(LocalDateTime.now());
        behaviorMapper.insert(behavior);
    }

    @Override
    public boolean isFavorited(Integer userId, String targetType, Integer targetId) {
        if (userId == null || targetId == null) return false;
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehavior::getUserId, userId)
               .eq(UserBehavior::getTargetType, targetType)
               .eq(UserBehavior::getTargetId, targetId)
               .eq(UserBehavior::getBehaviorType, "favorite")
               .last("LIMIT 1");
        return behaviorMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean toggleFavorite(Integer userId, String targetType, Integer targetId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehavior::getUserId, userId)
               .eq(UserBehavior::getTargetType, targetType)
               .eq(UserBehavior::getTargetId, targetId)
               .eq(UserBehavior::getBehaviorType, "favorite");
        Long count = behaviorMapper.selectCount(wrapper);
        if (count > 0) {
            behaviorMapper.delete(wrapper);
            return false;
        } else {
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setTargetType(targetType);
            behavior.setTargetId(targetId);
            behavior.setBehaviorType("favorite");
            behavior.setWeightScore(getWeight("favorite"));
            behavior.setCreateTime(LocalDateTime.now());
            behaviorMapper.insert(behavior);
            return true;
        }
    }

    private BigDecimal getWeight(String behaviorType) {
        return switch (behaviorType) {
            case "purchase" -> new BigDecimal("5.0");
            case "favorite" -> new BigDecimal("4.0");
            case "like", "comment" -> new BigDecimal("3.0");
            case "view", "browse", "read" -> new BigDecimal("1.0");
            default -> new BigDecimal("1.0");
        };
    }
}
