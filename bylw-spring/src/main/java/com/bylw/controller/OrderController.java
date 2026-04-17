package com.bylw.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.common.AuthUtil;
import com.bylw.common.Result;
import com.bylw.dto.OrderCreateDTO;
import com.bylw.dto.OrderDTO;
import com.bylw.entity.Food;
import com.bylw.entity.Order;
import com.bylw.service.ExpiryReminderService;
import com.bylw.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ExpiryReminderService expiryReminderService;

    @PostMapping
    public Result<?> create(HttpServletRequest request, @Valid @RequestBody OrderCreateDTO dto) {
        Integer userId = authUtil.getUserId(request);
        return Result.success(orderService.create(dto, userId));
    }

    @GetMapping("/{id}")
    public Result<?> getById(HttpServletRequest request, @PathVariable Integer id) {
        Integer userId = authUtil.getUserId(request);
        boolean isAdmin = "admin".equals(authUtil.getRole(request));
        OrderDTO order = orderService.getById(id);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (!isAdmin && !userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("无权查看此订单");
        }
        return Result.success(order);
    }

    @GetMapping("/my")
    public Result<?> listMy(HttpServletRequest request,
                            @RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer userId = authUtil.getUserId(request);
        return Result.success(orderService.listByUser(userId, pageNum, pageSize));
    }

    @GetMapping("/list")
    public Result<?> listAll(HttpServletRequest request,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String status) {
        authUtil.verifyAdmin(request);
        return Result.success(orderService.listAll(pageNum, pageSize, status));
    }

    @PutMapping("/status/{id}")
    public Result<?> updateStatus(HttpServletRequest request, @PathVariable Integer id, @RequestParam String status) {
        authUtil.verifyAdmin(request);
        return Result.success(orderService.updateStatus(id, status));
    }

    @PutMapping("/mock-pay/{id}")
    public Result<?> mockPay(HttpServletRequest request, @PathVariable Integer id) {
        Integer userId = authUtil.getUserId(request);
        OrderDTO order = orderService.getById(id);
        if (order == null) {
            return Result.success(false);
        }
        // 仅订单本人可以模拟支付
        if (!userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("无权操作此订单");
        }
        return Result.success(orderService.pay(id));
    }

    @PutMapping("/pay/{id}")
    public Result<?> pay(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        return Result.success(orderService.pay(id));
    }

    @PutMapping("/cancel/{id}")
    public Result<?> cancel(HttpServletRequest request, @PathVariable Integer id) {
        Integer userId = authUtil.getUserId(request);
        boolean isAdmin = "admin".equals(authUtil.getRole(request));
        OrderDTO order = orderService.getById(id);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        if (!isAdmin && !userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("无权取消此订单");
        }
        return Result.success(orderService.cancel(id));
    }

    @PutMapping("/accept/{id}")
    public Result<?> accept(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        return Result.success(orderService.acceptOrder(id));
    }

    @PutMapping("/reject/{id}")
    public Result<?> reject(HttpServletRequest request, @PathVariable Integer id,
                           @RequestParam(required = false) String reason) {
        authUtil.verifyAdmin(request);
        return Result.success(orderService.rejectOrder(id, reason));
    }

    @PutMapping("/dispatch-assign/{id}")
    public Result<?> dispatchAssign(HttpServletRequest request,
                                   @PathVariable Integer id,
                                   @RequestParam String courierName,
                                   @RequestParam String courierPhone) {
        authUtil.verifyAdmin(request);
        return Result.success(orderService.dispatchAssign(id, courierName, courierPhone));
    }

    @GetMapping("/recent")
    public Result<?> recent(HttpServletRequest request) {
        authUtil.verifyAdmin(request);
        return Result.success(orderService.getRecentOrders());
    }

    @GetMapping("/expiry-reminder")
    public Result<?> expiryReminder(HttpServletRequest request,
                                    @RequestParam(defaultValue = "24") Integer hoursAhead) {
        Integer userId = authUtil.getUserId(request);
        List<Food> expiringFoods = expiryReminderService.getExpiringFoods(userId, hoursAhead);
        return Result.success(expiringFoods);
    }
}
