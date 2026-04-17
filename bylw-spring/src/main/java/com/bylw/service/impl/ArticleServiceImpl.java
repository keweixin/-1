package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.entity.Article;
import com.bylw.entity.Recipe;
import com.bylw.entity.RecipeFavorite;
import com.bylw.mapper.ArticleMapper;
import com.bylw.mapper.RecipeMapper;
import com.bylw.mapper.RecipeFavoriteMapper;
import com.bylw.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private RecipeMapper recipeMapper;
    @Autowired
    private RecipeFavoriteMapper favoriteMapper;

    @Override
    public Page<Article> listArticles(Integer pageNum, Integer pageSize, String keyword) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1)
               .eq(Article::getDeleted, 0)
               .orderByDesc(Article::getPublishTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Article::getTitle, keyword);
        }
        return articleMapper.selectPage(page, wrapper);
    }

    @Override
    public Article getArticleById(Integer articleId) {
        return articleMapper.selectById(articleId);
    }

    @Override
    public Article save(Article article) {
        if (article.getArticleId() == null) {
            article.setPublishTime(LocalDateTime.now());
            article.setStatus(1);
            article.setDeleted(0);
            articleMapper.insert(article);
        } else {
            articleMapper.updateById(article);
        }
        return article;
    }

    @Override
    public boolean deleteArticle(Integer articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setDeleted(1);
            articleMapper.updateById(article);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateArticle(Article article) {
        return articleMapper.updateById(article) > 0;
    }

    @Override
    public boolean incrementReadCount(Integer articleId) {
        return articleMapper.update(null,
                new LambdaUpdateWrapper<Article>()
                        .eq(Article::getArticleId, articleId)
                        .setSql("read_count = read_count + 1")) > 0;
    }

    @Override
    public Page<Recipe> listRecipes(Integer pageNum, Integer pageSize, String keyword) {
        Page<Recipe> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Recipe::getStatus, 1)
               .eq(Recipe::getDeleted, 0)
               .orderByDesc(Recipe::getPublishTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Recipe::getTitle, keyword);
        }
        return recipeMapper.selectPage(page, wrapper);
    }

    @Override
    public Recipe getRecipeById(Integer recipeId) {
        return recipeMapper.selectById(recipeId);
    }

    @Override
    public Recipe saveRecipe(Recipe recipe) {
        if (recipe.getRecipeId() == null) {
            recipe.setPublishTime(LocalDateTime.now());
            recipe.setStatus(1);
            recipe.setDeleted(0);
            recipeMapper.insert(recipe);
        } else {
            recipeMapper.updateById(recipe);
        }
        return recipe;
    }

    @Override
    public boolean deleteRecipe(Integer recipeId) {
        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe != null) {
            recipe.setDeleted(1);
            recipeMapper.updateById(recipe);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateRecipe(Recipe recipe) {
        return recipeMapper.updateById(recipe) > 0;
    }

    @Override
    public boolean incrementCollectCount(Integer recipeId) {
        return recipeMapper.update(null,
                new LambdaUpdateWrapper<Recipe>()
                        .eq(Recipe::getRecipeId, recipeId)
                        .setSql("collect_count = collect_count + 1")) > 0;
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Integer userId, Integer recipeId) {
        LambdaQueryWrapper<RecipeFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeFavorite::getUserId, userId)
               .eq(RecipeFavorite::getRecipeId, recipeId);
        RecipeFavorite existing = favoriteMapper.selectOne(wrapper);

        if (existing != null) {
            favoriteMapper.deleteById(existing.getFavoriteId());
            // 原子减量，防止并发收藏导致计数为负
            recipeMapper.update(null,
                    new LambdaUpdateWrapper<Recipe>()
                            .eq(Recipe::getRecipeId, recipeId)
                            .apply("collect_count > 0")
                            .setSql("collect_count = collect_count - 1"));
            return false;
        } else {
            RecipeFavorite favorite = new RecipeFavorite();
            favorite.setUserId(userId);
            favorite.setRecipeId(recipeId);
            favorite.setCreateTime(LocalDateTime.now());
            favoriteMapper.insert(favorite);
            incrementCollectCount(recipeId);
            return true;
        }
    }

    @Override
    public List<RecipeFavorite> getFavorites(Integer userId) {
        LambdaQueryWrapper<RecipeFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecipeFavorite::getUserId, userId)
               .orderByDesc(RecipeFavorite::getCreateTime);
        return favoriteMapper.selectList(wrapper);
    }
}
