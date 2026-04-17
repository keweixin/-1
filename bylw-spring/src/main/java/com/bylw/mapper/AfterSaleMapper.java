package com.bylw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bylw.entity.AfterSale;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AfterSaleMapper extends BaseMapper<AfterSale> {

    @Select("SELECT reason_type AS reasonType, COUNT(*) AS cnt FROM food_after_sale GROUP BY reason_type")
    List<Map<String, Object>> selectReasonTypeStats();
}
