package com.bylw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bylw.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    default List<OrderItem> selectByOrderIds(List<Integer> orderIds) {
        return selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                .in(OrderItem::getOrderId, orderIds)
        );
    }
}
