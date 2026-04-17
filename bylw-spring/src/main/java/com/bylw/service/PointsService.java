package com.bylw.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.entity.PointsRecord;
import com.bylw.entity.PointsGoods;
import com.bylw.entity.PointsExchange;

import java.util.List;

public interface PointsService {
    PointsRecord earnPoints(Integer userId, String sourceType, Integer value, String description);
    PointsRecord spendPoints(Integer userId, String sourceType, Integer value);
    Integer getBalance(Integer userId);
    List<PointsRecord> getRecords(Integer userId);
    List<PointsRecord> getAllRecords();

    Page<PointsGoods> listGoods(Integer pageNum, Integer pageSize);
    PointsGoods getGoodsById(Integer goodsId);
    PointsGoods saveGoods(PointsGoods goods);
    boolean deleteGoods(Integer goodsId);

    PointsExchange exchange(Integer userId, Integer goodsId);
    Page<PointsExchange> listExchanges(Integer pageNum, Integer pageSize);

    boolean signIn(Integer userId);
}
