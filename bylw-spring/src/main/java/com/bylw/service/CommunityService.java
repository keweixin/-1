package com.bylw.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.entity.Post;
import com.bylw.entity.Comment;

public interface CommunityService {
    Post publish(Post post);
    Post getPostById(Integer postId);
    Page<Post> listPosts(Integer pageNum, Integer pageSize);
    Page<Post> listPosts(Integer pageNum, Integer pageSize, String auditStatus);
    boolean auditPost(Integer postId, String status);
    boolean deletePost(Integer postId, Integer userId);
    boolean likePost(Integer postId, Integer userId);
    boolean unlikePost(Integer postId, Integer userId);
    boolean hasUserLiked(Integer postId, Integer userId);

    Comment comment(Comment comment);
    Comment getCommentById(Integer commentId);
    Page<Comment> getComments(Integer postId, Integer pageNum, Integer pageSize);
    boolean deleteComment(Integer commentId);
    Page<Comment> listAllComments(Integer pageNum, Integer pageSize);
}
