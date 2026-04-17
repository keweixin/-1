package com.bylw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bylw.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT IFNULL(AVG(total_amount), 0) FROM food_order WHERE order_status = #{orderStatus} AND deleted = 0")
    Double selectAvgTotalAmountByStatus(@Param("orderStatus") String orderStatus);

    @Select("SELECT COUNT(*) FROM food_order WHERE order_status = #{orderStatus} AND deleted = 0")
    Integer selectCountByStatus(@Param("orderStatus") String orderStatus);

    @Select("SELECT IFNULL(SUM(oi.quantity), 0) FROM food_order_item oi INNER JOIN food_order o ON oi.order_id = o.order_id WHERE o.order_status = #{orderStatus} AND o.deleted = 0")
    Integer selectTotalQuantityByOrderStatus(@Param("orderStatus") String orderStatus);

    @Select("SELECT IFNULL(SUM(oi.quantity), 0) FROM food_order_item oi INNER JOIN food_order o ON oi.order_id = o.order_id WHERE o.order_status = #{orderStatus} AND o.deleted = 0 AND o.create_time >= #{startTime} AND o.create_time < #{endTime}")
    Integer selectTotalQuantityByStatusAndTimeRange(
            @Param("orderStatus") String orderStatus,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select("SELECT IFNULL(SUM(oi.quantity), 0) FROM food_order_item oi INNER JOIN food_order o ON oi.order_id = o.order_id WHERE o.order_status IN ('已退款', '已取消') AND o.deleted = 0 AND o.create_time >= #{startTime} AND o.create_time < #{endTime}")
    Integer selectWastedQuantityByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
