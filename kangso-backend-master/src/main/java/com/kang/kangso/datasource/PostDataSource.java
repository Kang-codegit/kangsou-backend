package com.kang.kangso.datasource;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kang.kangso.common.ErrorCode;
import com.kang.kangso.exception.BusinessException;
import com.kang.kangso.model.dto.post.PostQueryRequest;
import com.kang.kangso.model.entity.Picture;
import com.kang.kangso.model.entity.Post;
import com.kang.kangso.model.vo.PostVO;
import com.kang.kangso.service.PostService;
import javafx.geometry.Pos;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 帖子服务实现
 * @author 
 */
@Service
@Slf4j
public class PostDataSource implements DataSource<PostVO> {

    @Resource
    private PostService postService;

    @Override
    public Page<PostVO> doSearch(String searchText, long pageNum, long pageSize) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        long current = (pageNum - 1) * pageSize;
        String url = String.format("https://cn.bing.com/search?q=%s&first=%s",searchText,current);
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据获取异常");
        }
        Elements elements = doc.select(".b_algo");
        List<Post> postList = new ArrayList<>();
        for (Element element : elements) {
//            // 取图片地址（murl）
//            String m = element.select(".iusc").attr("m");
//            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
//            String murl = (String) map.get("murl");
            // 获取链接
            String href =element.select("a").attr("href");
            // 取标题
            String title = element.select("h2 a").text();
            // 取内容
            String content = element.select(".b_caption p").text();
            Post post=new Post();
            post.setTitle(title);
            post.setHref(href);
            post.setContent(content);
            post.setSearchText(searchText);
            postList.add(post);
            if (postList.size() >= pageSize) {
                break;
            }
        }
        Page<Post> postPage = new Page<>(pageNum, pageSize);
        postPage.setRecords(postList);
        return postService.getPostVOPage(postPage,request);
    }

//        PostQueryRequest postQueryRequest = new PostQueryRequest();
//        postQueryRequest.setSearchText(searchText);
//        postQueryRequest.setCurrent(pageNum);
//        postQueryRequest.setPageSize(pageSize);
//        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        HttpServletRequest request = servletRequestAttributes.getRequest();
//        Page<Post> postPage = postService.searchFromEs(postQueryRequest);
//        return postService.getPostVOPage(postPage, request);
//    }
}





