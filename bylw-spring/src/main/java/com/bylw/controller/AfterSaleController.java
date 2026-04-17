package com.bylw.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.common.AuthUtil;
import com.bylw.common.Result;
import com.bylw.entity.AfterSale;
import com.bylw.service.AfterSaleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/after-sale")
public class AfterSaleController {

    @Autowired
    private AfterSaleService afterSaleService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping
    public Result<?> apply(HttpServletRequest request, @RequestBody AfterSale afterSale) {
        afterSale.setUserId(authUtil.getUserId(request));
        return Result.success(afterSaleService.apply(afterSale));
    }

    @GetMapping("/{id}")
    public Result<?> getById(HttpServletRequest request, @PathVariable Integer id) {
        Integer userId = authUtil.getUserId(request);
        String role = authUtil.getRole(request);
        AfterSale afterSale = afterSaleService.getById(id);
        if (afterSale == null) {
            return Result.success(null);
        }
        if (!"admin".equals(role) && !userId.equals(afterSale.getUserId())) {
            throw new IllegalArgumentException("无权查看该售后记录");
        }
        return Result.success(afterSale);
    }

    @GetMapping("/list")
    public Result<?> listAll(HttpServletRequest request,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String status) {
        authUtil.verifyAdmin(request);
        return Result.success(afterSaleService.listAll(pageNum, pageSize, status));
    }

    @GetMapping("/my")
    public Result<?> listMy(HttpServletRequest request,
                            @RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer userId = authUtil.getUserId(request);
        return Result.success(afterSaleService.listByUser(userId, pageNum, pageSize));
    }

    @PutMapping("/handle/{id}")
    public Result<?> handle(HttpServletRequest request,
                            @PathVariable Integer id,
                            @RequestParam(required = false) String handleStatus,
                            @RequestParam(required = false) String handleResult,
                            @RequestParam(required = false) String result) {
        authUtil.verifyAdmin(request);
        String finalResult = handleResult != null ? handleResult : result;
        return Result.success(afterSaleService.handle(id, handleStatus, finalResult));
    }
}