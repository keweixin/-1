package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bylw.entity.Courier;
import com.bylw.entity.User;
import com.bylw.mapper.CourierMapper;
import com.bylw.mapper.UserMapper;
import com.bylw.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourierServiceImpl implements CourierService {

    @Autowired
    private CourierMapper courierMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Courier> listActive() {
        LambdaQueryWrapper<Courier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Courier::getStatus, 1).orderByAsc(Courier::getCreateTime);
        return courierMapper.selectList(wrapper);
    }

    @Override
    public List<Courier> listAll() {
        LambdaQueryWrapper<Courier> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Courier::getCreateTime);
        return courierMapper.selectList(wrapper);
    }

    @Override
    public Courier getById(Integer courierId) {
        return courierMapper.selectById(courierId);
    }

    @Override
    public boolean add(Courier courier) {
        if (courier.getCourierName() == null || courier.getCourierName().isBlank()) {
            throw new IllegalArgumentException("骑手姓名不能为空");
        }
        if (courier.getCourierPhone() == null || courier.getCourierPhone().isBlank()) {
            throw new IllegalArgumentException("骑手电话不能为空");
        }
        courier.setStatus(1);
        courier.setCreateTime(LocalDateTime.now());
        boolean ok = courierMapper.insert(courier) > 0;
        if (ok && courier.getUserId() != null) {
            syncCourierToUser(courier.getUserId(), courier.getCourierName(), courier.getCourierPhone());
        }
        return ok;
    }

    @Override
    public boolean update(Courier courier) {
        if (courier.getCourierId() == null) {
            throw new IllegalArgumentException("骑手ID不能为空");
        }
        Courier existing = courierMapper.selectById(courier.getCourierId());
        boolean ok = courierMapper.updateById(courier) > 0;
        if (ok) {
            Integer userId = courier.getUserId() != null ? courier.getUserId() : (existing != null ? existing.getUserId() : null);
            if (userId != null) {
                syncCourierToUser(userId, courier.getCourierName(), courier.getCourierPhone());
            }
        }
        return ok;
    }

    private void syncCourierToUser(Integer userId, String courierName, String courierPhone) {
        if (userId == null) return;
        User user = userMapper.selectById(userId);
        if (user != null) {
            if (courierName != null && !courierName.isBlank()) {
                user.setNickname(courierName);
            }
            if (courierPhone != null && !courierPhone.isBlank()) {
                user.setPhone(courierPhone);
            }
            userMapper.updateById(user);
        }
    }

    @Override
    public boolean delete(Integer courierId) {
        if (courierId == null) {
            throw new IllegalArgumentException("骑手ID不能为空");
        }
        LambdaUpdateWrapper<Courier> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Courier::getCourierId, courierId).set(Courier::getStatus, 0);
        return courierMapper.update(null, wrapper) > 0;
    }
}
