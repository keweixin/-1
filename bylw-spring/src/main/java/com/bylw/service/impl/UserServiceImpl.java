package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.entity.User;
import com.bylw.mapper.UserMapper;
import com.bylw.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<User> listUsers(Integer pageNum, Integer pageSize, String keyword) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(User::getUsername, keyword)
                   .or()
                   .like(User::getPhone, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    public User getUserById(Integer id) {
        return userMapper.selectById(id);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        User user = new User();
        user.setUserId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }
}
