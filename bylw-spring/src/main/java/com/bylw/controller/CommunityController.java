package com.bylw.controller;

import com.bylw.common.AuthUtil;
import com.bylw.common.Result;
import com.bylw.entity.Post;
import com.bylw.entity.Comment;
import com.bylw.mapper.PostMapper;
import com.bylw.service.CommunityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private PostMapper postMapper;

    @PostMapping("/post")
    public Result<?> publish(HttpServletRequest request, @RequestBody Post post) {
        post.setUserId(authUtil.getUserId(request));
        return Result.success(communityService.publish(post));
    }

    @GetMapping("/post/{id}")
    public Result<?> getPost(@PathVariable Integer id) {
        return Result.success(communityService.getPostById(id));
    }

    @GetMapping("/post/list")
    public Result<?> listPosts(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(communityService.listPosts(pageNum, pageSize));
    }

    @GetMapping("/post/admin/list")
    public Result<?> listPostsForAdmin(HttpServletRequest request,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String auditStatus) {
        authUtil.verifyAdmin(request);
        return Result.success(communityService.listPosts(pageNum, pageSize, auditStatus));
    }

    @PutMapping("/post/audit/{id}")
    public Result<?> auditPost(HttpServletRequest request, @PathVariable Integer id, @RequestParam String status) {
        authUtil.verifyAdmin(request);
        return Result.success(communityService.auditPost(id, status));
    }

    @DeleteMapping("/post/{id}")
    public Result<?> deletePost(HttpServletRequest request, @PathVariable Integer id) {
        Integer userId = authUtil.getUserId(request);
        authUtil.verifyAdmin(request);
        return Result.success(communityService.deletePost(id, userId));
    }

    @PostMapping("/post/like/{id}")
    public Result<?> likePost(HttpServletRequest request, @PathVariable Integer id) {
        Integer userId = authUtil.getUserId(request);
        return Result.success(communityService.likePost(id, userId));
    }

    @DeleteMapping("/post/like/{id}")
    public Result<?> unlikePost(HttpServletRequest request, @PathVariable Integer id) {
        Integer userId = authUtil.getUserId(request);
        return Result.success(communityService.unlikePost(id, userId));
    }

    @PostMapping("/comment")
    public Result<?> comment(HttpServletRequest request, @RequestBody Comment comment) {
        comment.setUserId(authUtil.getUserId(request));
        return Result.success(communityService.comment(comment));
    }

    @GetMapping("/comment/{postId}")
    public Result<?> getComments(@PathVariable Integer postId,
                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(communityService.getComments(postId, pageNum, pageSize));
    }

    @DeleteMapping("/comment/{id}")
    public Result<?> deleteComment(HttpServletRequest request, @PathVariable Integer id) {
        Integer userId = authUtil.getUserId(request);
        Comment comment = communityService.getCommentById(id);
        if (comment == null) {
            return Result.error(404, "评论不存在");
        }
        // 管理员可删除任何评论，普通用户只能删除自己的
        boolean isAdmin = authUtil.isAdmin(request);
        if (!isAdmin && !comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除他人的评论");
        }
        return Result.success(communityService.deleteComment(id));
    }

    @GetMapping("/comment/admin/list")
    public Result<?> listAllComments(HttpServletRequest request,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        authUtil.verifyAdmin(request);
        return Result.success(communityService.listAllComments(pageNum, pageSize));
    }

    @PutMapping("/post/recommend/{id}")
    public Result<?> toggleRecommend(HttpServletRequest request, @PathVariable Integer id) {
        authUtil.verifyAdmin(request);
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        post.setRecommended(post.getRecommended() != null && post.getRecommended() == 1 ? 0 : 1);
        postMapper.updateById(post);
        return Result.success(post.getRecommended());
    }
}