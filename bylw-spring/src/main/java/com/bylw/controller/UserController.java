package com.bylw.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.common.AuthUtil;
import com.bylw.common.Constants;
import com.bylw.common.Result;
import com.bylw.entity.User;
import com.bylw.mapper.UserMapper;
import com.bylw.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/count")
    public Result<?> countUsers(HttpServletRequest request) {
        authUtil.verifyAdmin(request);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0)
               .ne(User::getRole, "admin");
        long count = userMapper.selectCount(wrapper);
        return Result.success(count);
    }

    @GetMapping("/list")
    public Result<?> listUsers(HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer roleType) {
        authUtil.verifyAdmin(request);
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        if (roleType != null) {
            wrapper.eq(User::getRoleType, roleType);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<?> getUser(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        User user = userService.getUserById(id);
        if (user != null) user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/status/{id}")
    public Result<?> updateStatus(HttpServletRequest request, @PathVariable Integer id, @RequestParam Integer status) {
        authUtil.verifyAdmin(request);
        userService.updateStatus(id, status);
        return Result.success(true);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteUser(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success(true);
    }

    @PutMapping("/role-type/{id}")
    public Result<?> updateRoleType(HttpServletRequest request, @PathVariable Integer id, @RequestParam Integer roleType) {
        authUtil.verifyAdmin(request);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setRoleType(roleType);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success(true);
    }

    @PostMapping
    public Result<?> addUser(HttpServletRequest request, @RequestBody User user) {
        authUtil.verifyAdmin(request);
        if (user.getUsername() == null || user.getUsername().length() < 3) {
            throw new IllegalArgumentException("用户名长度不能少于3位");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername()).eq(User::getDeleted, 0);
        if (userMapper.selectOne(wrapper) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        user.setRole(Constants.ROLE_USER);
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return Result.success(true);
    }

    @GetMapping("/merchants")
    public Result<?> listMerchants(HttpServletRequest request) {
        authUtil.verifyAdmin(request);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0).eq(User::getRoleType, 3).orderByDesc(User::getCreateTime);
        var list = userMapper.selectList(wrapper);
        list.forEach(u -> u.setPassword(null));
        return Result.success(list);
    }

    @GetMapping("/couriers")
    public Result<?> listCouriers(HttpServletRequest request) {
        authUtil.verifyAdmin(request);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0).eq(User::getRoleType, 2).orderByDesc(User::getCreateTime);
        var list = userMapper.selectList(wrapper);
        list.forEach(u -> u.setPassword(null));
        return Result.success(list);
    }
}
