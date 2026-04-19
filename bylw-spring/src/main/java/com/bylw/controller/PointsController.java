package com.bylw.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.common.AuthUtil;
import com.bylw.common.Constants;
import com.bylw.common.Result;
import com.bylw.entity.PointsRecord;
import com.bylw.entity.PointsGoods;
import com.bylw.entity.PointsExchange;
import com.bylw.service.PointsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @Autowired
    private AuthUtil authUtil;

    @GetMapping("/balance")
    public Result<?> getBalance(HttpServletRequest request) {
        Integer userId = authUtil.getUserId(request);
        return Result.success(Map.of("balance", pointsService.getBalance(userId)));
    }

    @GetMapping("/history")
    public Result<?> getHistory(HttpServletRequest request) {
        Integer userId = authUtil.getUserId(request);
        return Result.success(pointsService.getRecords(userId));
    }

    /** 管理员查看所有积分记录 */
    @GetMapping("/history/all")
    public Result<?> getAllHistory(HttpServletRequest request) {
        authUtil.verifyAdmin(request);
        return Result.success(pointsService.getAllRecords());
    }

    @GetMapping("/goods")
    public Result<?> listGoods(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(pointsService.listGoods(pageNum, pageSize));
    }

    @GetMapping("/goods/{id}")
    public Result<?> getGoods(@PathVariable Integer id) {
        return Result.success(pointsService.getGoodsById(id));
    }

    @PostMapping("/exchange")
    public Result<?> exchange(HttpServletRequest request, @RequestBody Map<String, Integer> body) {
        Integer userId = authUtil.getUserId(request);
        Integer goodsId = body.get("goodsId");
        if (goodsId == null) {
            return Result.error("商品ID不能为空");
        }
        return Result.success(pointsService.exchange(userId, goodsId));
    }

    @PostMapping("/sign-in")
    public Result<?> signIn(HttpServletRequest request) {
        Integer userId = authUtil.getUserId(request);
        boolean success = pointsService.signIn(userId);
        return Result.success(Map.of("success", success, "points", Constants.SIGN_IN_POINTS));
    }

    @GetMapping("/exchange/list")
    public Result<?> listExchanges(HttpServletRequest request,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        authUtil.verifyAdmin(request);
        return Result.success(pointsService.listExchanges(pageNum, pageSize));
    }

    @PostMapping("/goods")
    public Result<?> saveGoods(HttpServletRequest request, @RequestBody PointsGoods goods) {
        authUtil.verifyAdmin(request);
        return Result.success(pointsService.saveGoods(goods));
    }

    @PutMapping("/goods")
    public Result<?> updateGoods(HttpServletRequest request, @RequestBody PointsGoods goods) {
        authUtil.verifyAdmin(request);
        if (goods.getGoodsId() == null) {
            return Result.error("商品ID不能为空");
        }
        return Result.success(pointsService.saveGoods(goods));
    }

    @DeleteMapping("/goods/{id}")
    public Result<?> deleteGoods(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        return Result.success(pointsService.deleteGoods(id));
    }
}