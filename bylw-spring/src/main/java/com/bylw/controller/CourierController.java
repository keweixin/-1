package com.bylw.controller;

import com.bylw.common.AuthUtil;
import com.bylw.common.Result;
import com.bylw.entity.Courier;
import com.bylw.service.CourierService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courier")
public class CourierController {

    @Autowired
    private CourierService courierService;

    @Autowired
    private AuthUtil authUtil;

    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false, defaultValue = "false") Boolean all) {
        List<Courier> couriers = Boolean.TRUE.equals(all) ? courierService.listAll() : courierService.listActive();
        return Result.success(couriers);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Integer id) {
        return Result.success(courierService.getById(id));
    }

    @PostMapping
    public Result<?> add(@RequestBody Courier courier, HttpServletRequest request) {
        authUtil.verifyAdmin(request);
        return Result.success(courierService.add(courier));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Integer id, @RequestBody Courier courier, HttpServletRequest request) {
        authUtil.verifyAdmin(request);
        courier.setCourierId(id);
        return Result.success(courierService.update(courier));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id, HttpServletRequest request) {
        authUtil.verifyAdmin(request);
        return Result.success(courierService.delete(id));
    }
}