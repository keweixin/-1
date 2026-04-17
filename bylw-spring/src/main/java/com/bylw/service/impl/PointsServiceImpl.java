package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.common.Constants;
import com.bylw.entity.*;
import com.bylw.mapper.*;
import com.bylw.service.PointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class PointsServiceImpl implements PointsService {

    @Autowired
    private PointsRecordMapper recordMapper;
    @Autowired
    private PointsGoodsMapper goodsMapper;
    @Autowired
    private PointsExchangeMapper exchangeMapper;
    @Autowired
    private UserMapper userMapper;

    private Integer getBalanceUnsafe(Integer userId) {
        return recordMapper.selectSumByUserId(userId);
    }

    @Override
    @Transactional
    public PointsRecord earnPoints(Integer userId, String sourceType, Integer value, String description) {
        lockUser(userId);
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setChangeType(Constants.CHANGE_TYPE_EARN);
        record.setChangeValue(value);
        record.setSourceType(sourceType);
        record.setRemark(description);
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PointsRecord spendPoints(Integer userId, String sourceType, Integer value) {
        lockUser(userId);
        Integer balance = getBalanceUnsafe(userId);
        if (balance < value) {
            throw new IllegalArgumentException("积分不足");
        }
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setChangeType(Constants.CHANGE_TYPE_SPEND);
        record.setChangeValue(-value);
        record.setSourceType(sourceType);
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);
        return record;
    }

    @Override
    public Integer getBalance(Integer userId) {
        return getBalanceUnsafe(userId);
    }

    @Override
    public List<PointsRecord> getRecords(Integer userId) {
        LambdaQueryWrapper<PointsRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRecord::getUserId, userId)
               .orderByDesc(PointsRecord::getCreateTime);
        return recordMapper.selectList(wrapper);
    }

    @Override
    public List<PointsRecord> getAllRecords() {
        LambdaQueryWrapper<PointsRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PointsRecord::getCreateTime);
        return recordMapper.selectList(wrapper);
    }

    @Override
    public Page<PointsGoods> listGoods(Integer pageNum, Integer pageSize) {
        Page<PointsGoods> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PointsGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsGoods::getStatus, 1)
               .eq(PointsGoods::getDeleted, 0)
               .orderByAsc(PointsGoods::getPointCost);
        return goodsMapper.selectPage(page, wrapper);
    }

    @Override
    public PointsGoods getGoodsById(Integer goodsId) {
        return goodsMapper.selectById(goodsId);
    }

    @Override
    public PointsGoods saveGoods(PointsGoods goods) {
        if (goods.getGoodsId() == null) {
            goods.setDeleted(0);
            goodsMapper.insert(goods);
        } else {
            goodsMapper.updateById(goods);
        }
        return goods;
    }

    @Override
    public boolean deleteGoods(Integer goodsId) {
        PointsGoods goods = goodsMapper.selectById(goodsId);
        if (goods != null) {
            goods.setDeleted(1);
            goodsMapper.updateById(goods);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PointsExchange exchange(Integer userId, Integer goodsId) {
        // 1. 校验商品有效性
        PointsGoods goods = goodsMapper.selectById(goodsId);
        if (goods == null || (goods.getDeleted() != null && goods.getDeleted() == 1)
                || goods.getStatus() == null || goods.getStatus() != 1) {
            throw new IllegalArgumentException("商品不存在");
        }

        // 2. 校验用户积分充足（先于库存扣减，更符合业务语义）
        lockUser(userId);
        Integer balance = getBalanceUnsafe(userId);
        Integer cost = goods.getPointCost();
        if (balance < cost) {
            throw new IllegalArgumentException("积分不足，当前积分：" + balance);
        }

        // 3. 原子扣减库存（条件判断 + 乐观锁，防止并发超卖）
        LambdaUpdateWrapper<PointsGoods> stockWrapper = new LambdaUpdateWrapper<>();
        stockWrapper.eq(PointsGoods::getGoodsId, goodsId)
                .ge(PointsGoods::getStockQty, 1)
                .setSql("stock_qty = stock_qty - 1");
        int updated = goodsMapper.update(null, stockWrapper);
        if (updated == 0) {
            throw new IllegalArgumentException("商品库存不足或已被并发修改");
        }

        // 4. 扣减用户积分
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setChangeType(Constants.CHANGE_TYPE_SPEND);
        record.setChangeValue(-cost);
        record.setSourceType(Constants.SOURCE_EXCHANGE);
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);

        // 5. 创建兑换记录
        PointsExchange exchange = new PointsExchange();
        exchange.setUserId(userId);
        exchange.setGoodsId(goodsId);
        exchange.setExchangeStatus("待发货");
        exchange.setCreateTime(LocalDateTime.now());
        exchangeMapper.insert(exchange);

        return exchange;
    }

    @Override
    @Transactional
    public boolean signIn(Integer userId) {
        lockUser(userId);
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LambdaQueryWrapper<PointsRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PointsRecord::getUserId, userId)
               .eq(PointsRecord::getSourceType, Constants.SOURCE_SIGN)
               .ge(PointsRecord::getCreateTime, startOfDay);
        long count = recordMapper.selectCount(wrapper);
        if (count > 0) {
            throw new IllegalArgumentException("今日已签到，明日再来吧！");
        }
        earnPoints(userId, Constants.SOURCE_SIGN, Constants.SIGN_IN_POINTS, "每日签到");
        return true;
    }

    @Override
    public Page<PointsExchange> listExchanges(Integer pageNum, Integer pageSize) {
        Page<PointsExchange> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<PointsExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(PointsExchange::getCreateTime);
        return exchangeMapper.selectPage(page, wrapper);
    }

    private void lockUser(Integer userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserId, userId)
               .eq(User::getDeleted, 0)
               .last("FOR UPDATE");
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
    }
}
