package com.kang.kangso.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kang.kangso.model.dto.post.PostQueryRequest;
import com.kang.kangso.model.entity.Post;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 帖子服务测试
 *
 * @author kang
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@SpringBootTest
class PostServiceTest {

    @Resource
    private PostService postService;

    @Test
    void searchFromEs() {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        postQueryRequest.setUserId(1L);
        Page<Post> postPage = postService.searchFromEs(postQueryRequest);
        Assertions.assertNotNull(postPage);
    }

}