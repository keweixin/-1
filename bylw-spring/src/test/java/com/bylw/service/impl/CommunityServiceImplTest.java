package com.bylw.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bylw.entity.Post;
import com.bylw.mapper.CommentMapper;
import com.bylw.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityServiceImplTest {

    @Mock
    private PostMapper postMapper;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommunityServiceImpl communityService;

    @Test
    void adminListShouldSupportOptionalAuditStatus() {
        Post post = new Post();
        post.setPostId(1);

        Page<Post> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(post));
        when(postMapper.selectPage(any(), any())).thenReturn(page);

        Page<Post> result = communityService.listPosts(1, 10, null);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getPostId()).isEqualTo(1);
    }
}
