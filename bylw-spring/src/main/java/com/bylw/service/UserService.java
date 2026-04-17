package com.bylw.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.entity.User;

public interface UserService {
    Page<User> listUsers(Integer pageNum, Integer pageSize, String keyword);
    User getUserById(Integer id);
    void updateStatus(Integer id, Integer status);
}
