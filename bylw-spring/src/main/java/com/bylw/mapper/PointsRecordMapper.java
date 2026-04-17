package com.bylw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bylw.entity.PointsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface PointsRecordMapper extends BaseMapper<PointsRecord> {

    @Select("SELECT IFNULL(SUM(change_value), 0) FROM points_record WHERE user_id = #{userId}")
    Integer selectSumByUserId(@Param("userId") Integer userId);

    @Select("SELECT IFNULL(SUM(change_value), 0) FROM points_record WHERE change_type = #{changeType}")
    Integer selectSumByChangeType(@Param("changeType") String changeType);

    @Select("SELECT IFNULL(SUM(change_value), 0) FROM points_record WHERE change_type = #{changeType} AND create_time >= #{startTime} AND create_time < #{endTime}")
    Integer selectSumByChangeTypeAndTimeRange(
            @Param("changeType") String changeType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
