package com.bylw.controller;

import com.bylw.common.AuthUtil;
import com.bylw.common.Result;
import com.bylw.entity.*;
import com.bylw.mapper.*;
import com.bylw.service.SystemService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Autowired
    private SystemService systemService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuthUtil authUtil;

    // 公告管理
    @GetMapping("/notice/list")
    public Result<?> getNotices() {
        return Result.success(systemService.getNotices());
    }

    @PostMapping("/notice")
    public Result<?> publishNotice(HttpServletRequest request, @RequestBody Notice notice) {
        authUtil.verifyAdmin(request);
        return Result.success(systemService.publishNotice(notice));
    }

    @PutMapping("/notice/{id}")
    public Result<?> updateNotice(HttpServletRequest request, @PathVariable Integer id, @RequestBody Notice notice) {
        authUtil.verifyAdmin(request);
        notice.setNoticeId(id);
        return Result.success(systemService.updateNotice(notice));
    }

    @DeleteMapping("/notice/{id}")
    public Result<?> deleteNotice(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        return Result.success(systemService.deleteNotice(id));
    }

    // 轮播图管理
    @GetMapping("/banner/list")
    public Result<?> getBanners() {
        return Result.success(systemService.getBanners());
    }

    @PostMapping("/banner")
    public Result<?> saveBanner(HttpServletRequest request, @RequestBody Banner banner) {
        authUtil.verifyAdmin(request);
        return Result.success(systemService.saveBanner(banner));
    }

    @PutMapping("/banner/{id}")
    public Result<?> updateBanner(HttpServletRequest request, @PathVariable Integer id, @RequestBody Banner banner) {
        authUtil.verifyAdmin(request);
        banner.setBannerId(id);
        return Result.success(systemService.updateBanner(banner));
    }

    @DeleteMapping("/banner/{id}")
    public Result<?> deleteBanner(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        return Result.success(systemService.deleteBanner(id));
    }

    // 友情链接管理
    @GetMapping("/friend-link/list")
    public Result<?> getFriendLinks() {
        return Result.success(systemService.getFriendLinks());
    }

    @PostMapping("/friend-link")
    public Result<?> saveFriendLink(HttpServletRequest request, @RequestBody FriendLink link) {
        authUtil.verifyAdmin(request);
        return Result.success(systemService.saveFriendLink(link));
    }

    @PutMapping("/friend-link/{id}")
    public Result<?> updateFriendLink(HttpServletRequest request, @PathVariable Integer id, @RequestBody FriendLink link) {
        authUtil.verifyAdmin(request);
        link.setLinkId(id);
        return Result.success(systemService.updateFriendLink(link));
    }

    @DeleteMapping("/friend-link/{id}")
    public Result<?> deleteFriendLink(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        return Result.success(systemService.deleteFriendLink(id));
    }

    // 关于我们管理
    @GetMapping("/about")
    public Result<?> getAboutUs() {
        return Result.success(systemService.getAboutUs());
    }

    @PutMapping("/about")
    public Result<?> updateAboutUs(HttpServletRequest request, @RequestBody AboutUs aboutUs) {
        authUtil.verifyAdmin(request);
        return Result.success(systemService.updateAboutUs(aboutUs));
    }

    // 用户管理
    @GetMapping("/user/list")
    public Result<?> listUsers(HttpServletRequest request) {
        authUtil.verifyAdmin(request);
        var users = userMapper.selectList(null);
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }
}