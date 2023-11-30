package com.kang.kangso.datasource;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kang.kangso.common.ErrorCode;
import com.kang.kangso.constant.CommonConstant;
import com.kang.kangso.exception.BusinessException;
import com.kang.kangso.model.dto.post.PostQueryRequest;
import com.kang.kangso.model.entity.Picture;
import com.kang.kangso.model.entity.Post;
import com.kang.kangso.model.vo.PostVO;
import com.kang.kangso.service.PostService;
import javafx.geometry.Pos;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 帖子服务实现
 *
 * @author
 */
@Service
@Slf4j
public class PostDataSource implements DataSource<PostVO> {

    @Resource
    private PostService postService;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Page<PostVO> doSearch(String searchText, long pageNum, long pageSize) {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        postQueryRequest.setSearchText(searchText);
        postQueryRequest.setCurrent(pageNum);
        postQueryRequest.setPageSize(pageSize);
        List<Post> PostList = postService.searchFromBY(searchText, pageNum, pageSize);
        for(Post post:PostList){
            Post existPost=postService.getByTitle(post.getTitle());
            if(existPost!=null){
                // 存在相同 title 的记录，更新 updateTime
                existPost.setUpdateTime(post.getUpdateTime());
                // 其他可能需要更新的字段也在这里更新
                // 执行更新操作
                boolean b=postService.updateById(existPost);
                if(b){
                    log.info("数据更新成功");
                }else {
                    log.error("数据更新失败");
                }
            }else{
                boolean b=postService.save(post);
                if(b){
                    log.info("数据插入成功");
                }else {
                    log.error("数据插入失败");
                }
            }
        }
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        Page<Post> postPage = postService.searchFromEs(postQueryRequest);
        return postService.getPostVOPage(postPage, request);
    }
    
}




    //ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//
//        long current = (pageNum - 1) * pageSize;
//        String encodedSearchText;
//        try {
//            encodedSearchText = URLEncoder.encode(searchText, "UTF-8").replace("+", "%20");
//        } catch (UnsupportedEncodingException e) {
//            // 处理异常
//            encodedSearchText = searchText;
//        }
//
//        // 构建URL
//        String url = String.format("https://cn.bing.com/search?q=%s&first=%s", encodedSearchText, current);
//        Document doc = null;
//        try {
//            doc = Jsoup.connect(url).get();
//        } catch (IOException e) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据获取异常");
//        }
//
//        Elements elements = doc.select(".b_algo");
//        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
//        List<PostVO> postList = new ArrayList<>();
//         for (Element element : elements) {
//            // 获取链接
//            String href = element.select("a").attr("href");
//            // 取标题
//            String title = element.select("h2 a").text();
//            // 取内容
//            String content = element.select(".b_caption p").text();
//            // 设置高亮
//            HighlightBuilder.Field titleField = new HighlightBuilder.Field("title");
//            titleField.preTags(CommonConstant.PRE_TAG);
//            titleField.postTags(CommonConstant.POST_TAG);
//            HighlightBuilder.Field contentField = new HighlightBuilder.Field("constant");
//            contentField.preTags(CommonConstant.PRE_TAG);
//            contentField.postTags(CommonConstant.POST_TAG);
//            searchQueryBuilder.withHighlightFields(titleField, contentField);
//            PostVO post = new PostVO();
//            post.setTitle(title);
//            post.setHref(href);
//            post.setContent(content);
//            post.setSearchText(searchText);
//            postList.add(post);
////            if (postList.size() >= pageSize) {
////                break;
////            }
//        }
//        try {
//            SearchHits<PostVO> searchHits = elasticsearchRestTemplate.search(searchQueryBuilder.build(), PostVO.class);
//            if (searchHits.getTotalHits() > 0) {
//                return convertToHighlightedPage(searchHits, pageNum, pageSize);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        // 如果搜索失败或者没有结果，使用原来的逻辑
//        Page<PostVO> postPage = new Page<>(pageNum, pageSize);
//        postPage.setRecords(postList);
//        return postPage;


