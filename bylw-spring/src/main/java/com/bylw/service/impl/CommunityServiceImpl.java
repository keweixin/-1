package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.common.Constants;
import com.bylw.entity.Post;
import com.bylw.entity.Comment;
import com.bylw.entity.UserLike;
import com.bylw.mapper.PostMapper;
import com.bylw.mapper.CommentMapper;
import com.bylw.mapper.UserLikeMapper;
import com.bylw.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CommunityServiceImpl implements CommunityService {

    private static final Logger log = LoggerFactory.getLogger(CommunityServiceImpl.class);

    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private UserLikeMapper userLikeMapper;

    @Override
    public Post publish(Post post) {
        post.setAuditStatus(Constants.AUDIT_STATUS_PENDING);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreateTime(LocalDateTime.now());
        post.setDeleted(0);
        postMapper.insert(post);
        return post;
    }

    @Override
    public Post getPostById(Integer postId) {
        return postMapper.selectById(postId);
    }

    @Override
    public Page<Post> listPosts(Integer pageNum, Integer pageSize) {
        return listPosts(pageNum, pageSize, Constants.AUDIT_STATUS_APPROVED);
    }

    @Override
    public Page<Post> listPosts(Integer pageNum, Integer pageSize, String auditStatus) {
        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getDeleted, 0)
               .orderByDesc(Post::getCreateTime);
        if (auditStatus != null && !auditStatus.isEmpty()) {
            wrapper.eq(Post::getAuditStatus, auditStatus);
        }
        return postMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean auditPost(Integer postId, String status) {
        Post post = postMapper.selectById(postId);
        if (post != null) {
            post.setAuditStatus(status);
            postMapper.updateById(post);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deletePost(Integer postId, Integer userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return false;
        }
        post.setDeleted(1);
        postMapper.updateById(post);
        log.info("[deletePost] postId={}, operatorUserId={}", postId, userId);
        return true;
    }

    @Override
    public boolean likePost(Integer postId, Integer userId) {
        LambdaQueryWrapper<UserLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(UserLike::getUserId, userId)
                   .eq(UserLike::getTargetType, "post")
                   .eq(UserLike::getTargetId, postId);
        Long existingLike = userLikeMapper.selectCount(likeWrapper);
        if (existingLike > 0) {
            return false;
        }
        Post post = postMapper.selectById(postId);
        if (post != null) {
            UserLike like = new UserLike();
            like.setUserId(userId);
            like.setTargetType("post");
            like.setTargetId(postId);
            like.setCreateTime(LocalDateTime.now());
            userLikeMapper.insert(like);
            // 原子 +1，防止并发点赞计数丢失
            postMapper.update(null,
                    new LambdaUpdateWrapper<Post>()
                            .eq(Post::getPostId, postId)
                            .setSql("like_count = like_count + 1"));
            return true;
        }
        return false;
    }

    @Override
    public boolean unlikePost(Integer postId, Integer userId) {
        LambdaQueryWrapper<UserLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(UserLike::getUserId, userId)
                   .eq(UserLike::getTargetType, "post")
                   .eq(UserLike::getTargetId, postId);
        UserLike existingLike = userLikeMapper.selectOne(likeWrapper);
        if (existingLike == null) {
            return false;
        }
        userLikeMapper.deleteById(existingLike.getLikeId());
        Post post = postMapper.selectById(postId);
        if (post != null) {
            // 原子减量更新，乐观锁防负数
            LambdaUpdateWrapper<Post> unlikeWrapper = new LambdaUpdateWrapper<>();
            unlikeWrapper.eq(Post::getPostId, postId)
                    .apply("like_count > 0")
                    .setSql("like_count = like_count - 1");
            postMapper.update(null, unlikeWrapper);
            return true;
        }
        return false;
    }

    @Override
    public boolean hasUserLiked(Integer postId, Integer userId) {
        LambdaQueryWrapper<UserLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(UserLike::getUserId, userId)
                   .eq(UserLike::getTargetType, "post")
                   .eq(UserLike::getTargetId, postId);
        return userLikeMapper.selectCount(likeWrapper) > 0;
    }

    @Override
    @Transactional
    public Comment comment(Comment comment) {
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        comment.setDeleted(0);
        commentMapper.insert(comment);

        Post post = postMapper.selectById(comment.getPostId());
        if (post != null) {
            LambdaUpdateWrapper<Post> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Post::getPostId, comment.getPostId())
                   .setSql("comment_count = comment_count + 1");
            postMapper.update(new Post(), wrapper);
        }
        return comment;
    }

    @Override
    public Comment getCommentById(Integer commentId) {
        return commentMapper.selectById(commentId);
    }

    @Override
    public Page<Comment> getComments(Integer postId, Integer pageNum, Integer pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
               .eq(Comment::getDeleted, 0)
               .orderByAsc(Comment::getCreateTime);
        return commentMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean deleteComment(Integer commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setDeleted(1);
            commentMapper.updateById(comment);
            return true;
        }
        return false;
    }
}
