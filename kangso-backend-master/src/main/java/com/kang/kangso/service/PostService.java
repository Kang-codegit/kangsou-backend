package com.kang.kangso.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kang.kangso.model.dto.post.PostQueryRequest;
import com.kang.kangso.model.entity.Post;
import com.kang.kangso.model.vo.PostVO;
import org.springframework.data.elasticsearch.core.SearchHits;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子服务
 *
 * @author kang
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface PostService extends IService<Post> {

    Post getByTitle(String title);

    /**
     * 校验
     *
     * @param post
     * @param add
     */
    void validPost(Post post, boolean add);

    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest);

    /**
     * 从必应查询文章
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    List<Post> searchFromBY(String searchText, long pageNum, long pageSize);

    Page<PostVO> convertToHighlightedPage(SearchHits<PostVO> searchHits, long pageNum, long pageSize);

    /**
     * 从 ES 查询
     *
     * @param postQueryRequest
     * @return
     */
    Page<Post> searchFromEs(PostQueryRequest postQueryRequest);

    /**
     * 获取帖子封装
     *
     * @param post
     * @param request
     * @return
     */
    PostVO getPostVO(Post post, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param postPage
     * @param request
     * @return
     */
    Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request);

    /**
     * 分页查询帖子
     * @param postQueryRequest
     * @param request
     * @return
     */
    Page<PostVO> listPostVOByPage(PostQueryRequest postQueryRequest, HttpServletRequest request);
}
