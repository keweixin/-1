package com.bylw.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.entity.AfterSale;

public interface AfterSaleService {
    AfterSale apply(AfterSale afterSale);
    AfterSale getById(Integer afterSaleId);
    Page<AfterSale> listAll(Integer pageNum, Integer pageSize, String status);
    Page<AfterSale> listByUser(Integer userId, Integer pageNum, Integer pageSize);
    boolean handle(Integer afterSaleId, String handleStatus, String handleResult);
}
