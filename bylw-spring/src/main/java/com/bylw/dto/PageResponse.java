package com.bylw.dto;

import lombok.Data;
import java.util.List;

/**
 * 通用分页响应，避免 MyBatis-Plus Page<OrderDTO> 泛型序列化问题
 */
@Data
public class PageResponse<T> {
    private List<T> records;
    private long total;
    private long size;
    private long current;

    public PageResponse() {}

    public PageResponse(List<T> records, long total, long size, long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
    }
}
