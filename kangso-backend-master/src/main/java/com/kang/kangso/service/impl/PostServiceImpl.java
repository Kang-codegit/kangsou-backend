package com.kang.kangso.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.kang.kangso.common.ErrorCode;
import com.kang.kangso.constant.CommonConstant;
import com.kang.kangso.exception.BusinessException;
import com.kang.kangso.exception.ThrowUtils;
import com.kang.kangso.mapper.PostFavourMapper;
import com.kang.kangso.mapper.PostMapper;
import com.kang.kangso.mapper.PostThumbMapper;
import com.kang.kangso.model.dto.post.PostEsDTO;
import com.kang.kangso.model.dto.post.PostEsHighlightData;
import com.kang.kangso.model.dto.post.PostQueryRequest;
import com.kang.kangso.model.entity.Post;
import com.kang.kangso.model.entity.PostFavour;
import com.kang.kangso.model.entity.PostThumb;
import com.kang.kangso.model.entity.User;
import com.kang.kangso.model.vo.PostVO;
import com.kang.kangso.model.vo.UserVO;
import com.kang.kangso.service.PostService;
import com.kang.kangso.service.UserService;
import com.kang.kangso.utils.SqlUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

/**
 * 帖子服务实现
 *
 * @author kang
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final static Gson GSON = new Gson();

    @Resource
    private UserService userService;

    @Resource
    private PostThumbMapper postThumbMapper;
    
    @Resource
    private PostMapper postMapper;
    
    @Resource
    private PostFavourMapper postFavourMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Post getByTitle(String title){
        return postMapper.getByTitle(title);
    }
    
    @Override
    public void validPost(Post post, boolean add) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = post.getTitle();
        String content = post.getContent();
        String tags = post.getTags();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param postQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        Long userId = postQueryRequest.getUserId();
        Long notId = postQueryRequest.getNotId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.like("title", searchText).or().like("content", searchText);
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollectionUtils.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public List<Post> searchFromBY(String searchText, long pageNum, long pageSize) {
        long current = (pageNum - 1) * pageSize;
        String encodedSearchText;
        try {
            encodedSearchText = URLEncoder.encode(searchText, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            // 处理异常
            encodedSearchText = searchText;
        }
        // 构建URL
        String url = String.format("https://cn.bing.com/search?q=%s&first=%s", encodedSearchText, current);
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据获取异常");
        }

        Elements elements = doc.select(".b_algo");
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        List<Post> postList = new ArrayList<>();
        for (Element element : elements) {
            // 获取链接
            String href = element.select("a").attr("href");
            // 取标题
            String title = element.select("h2 a").text();
            // 取内容
            String content = element.select(".b_caption p").text();
            // 设置高亮
            HighlightBuilder.Field titleField = new HighlightBuilder.Field("title");
            titleField.preTags(CommonConstant.PRE_TAG);
            titleField.postTags(CommonConstant.POST_TAG);
            HighlightBuilder.Field contentField = new HighlightBuilder.Field("constant");
            contentField.preTags(CommonConstant.PRE_TAG);
            contentField.postTags(CommonConstant.POST_TAG);
            searchQueryBuilder.withHighlightFields(titleField, contentField);
            Post post = new Post();
            post.setTitle(title);
            post.setHref(href);
            post.setContent(content);
            post.setSearchText(searchText);
            postList.add(post);
        }
        return postList;

    }

    //                if (postList.size() >= pageSize) {
//                break;
//            }
//        try {
//            SearchHits<Post> searchHits = elasticsearchRestTemplate.search(searchQueryBuilder.build(), Post.class);
//            if (searchHits.getTotalHits() > 0) {
//                return convertToHighlightedPage(searchHits, pageNum, pageSize);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//     如果搜索失败或者没有结果，使用原来的逻辑
//        Page<Post> postPage = new Page<>(pageNum, pageSize);
//        postPage.setRecords(postList); 
//        
    @Override
    public Page<PostVO> convertToHighlightedPage(SearchHits<PostVO> searchHits, long pageNum, long pageSize) {
        List<PostVO> highlightedPosts = searchHits.getSearchHits().stream().map(hit -> {
            PostVO postVO = hit.getContent();
            // Highlight title
            List<String> titleHighlights = hit.getHighlightFields().get("title");
            if (CollectionUtils.isNotEmpty(titleHighlights)) {
                postVO.setTitle(titleHighlights.get(0));
            }
            // Highlight content
            List<String> contentHighlights = hit.getHighlightFields().get("content");
            if (CollectionUtils.isNotEmpty(contentHighlights)) {
                postVO.setContent(contentHighlights.get(0));
            }
            return postVO;
        }).collect(Collectors.toList());
        Page<PostVO> highlightedPage = new Page<>(pageNum, pageSize);
        highlightedPage.setRecords(highlightedPosts);
        return highlightedPage;
    }

    @Override
    public Page<Post> searchFromEs(PostQueryRequest postQueryRequest) {
        // 获取查询数据
        Long id = postQueryRequest.getId();
        Long notId = postQueryRequest.getNotId();
        String searchText = postQueryRequest.getSearchText();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        List<String> orTagList = postQueryRequest.getOrTags();
        Long userId = postQueryRequest.getUserId();
        // es 起始页为 0
        long current = postQueryRequest.getCurrent() - 1;
        long pageSize = postQueryRequest.getPageSize();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }
        // 必须包含所有标签
        if (CollectionUtils.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
            }
        }
        // 包含任何一个标签即可
        if (CollectionUtils.isNotEmpty(orTagList)) {
            BoolQueryBuilder orTagBoolQueryBuilder = QueryBuilders.boolQuery();
            for (String tag : orTagList) {
                orTagBoolQueryBuilder.should(QueryBuilders.termQuery("tags", tag));
            }
            orTagBoolQueryBuilder.minimumShouldMatch(1);
            boolQueryBuilder.filter(orTagBoolQueryBuilder);
        }
        // 按关键词检索 满足其一√
        if (StringUtils.isNotBlank(searchText)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按标题检索
        if (StringUtils.isNotBlank(title)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", title));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按内容检索
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", content));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 搜索关键词高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("*").preTags("<font color='#eea6b7'>")
                .postTags("</font>");
        ; //所有的字段都高亮
        highlightBuilder.requireFieldMatch(false);//如果要多个字段高亮,这项要为false

        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 分页
        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withHighlightBuilder(highlightBuilder)
                .withPageable(pageRequest).withSorts(sortBuilder).build();
        SearchHits<PostEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, PostEsDTO.class);

        Page<Post> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Post> resourceList = new ArrayList<>();
        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
        if (searchHits.hasSearchHits()) {
            List<SearchHit<PostEsDTO>> searchHitList = searchHits.getSearchHits();
            // 搜索关键词高亮
            Map<Long, PostEsHighlightData> highlightDataMap = new HashMap<>();
            for (SearchHit hit : searchHits.getSearchHits()) {
                PostEsHighlightData data = new PostEsHighlightData();
                data.setId(Long.valueOf(hit.getId()));
                if (hit.getHighlightFields().get("title") != null) {
                    String highlightTitle = String.valueOf(hit.getHighlightFields().get("title"));
                    data.setTitle(highlightTitle.substring(1, highlightTitle.length() - 1));
                    System.out.println(data.getTitle());
                }
                if (hit.getHighlightFields().get("content") != null) {
                    String highlightContent = String.valueOf(hit.getHighlightFields().get("content"));
                    data.setContent(highlightContent.substring(1, highlightContent.length() - 1));
                    System.out.println(data.getContent());
                }
                highlightDataMap.put(data.getId(), data);
            }
            // id列表
            List<Long> postIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
                    .collect(Collectors.toList());
            // 根据id查找数据集
            List<Post> postList = baseMapper.selectBatchIds(postIdList);
            if (postList != null) {
                Map<Long, List<Post>> idPostMap = postList.stream().collect(Collectors.groupingBy(Post::getId));
                postIdList.forEach(postId -> {
                    if (idPostMap.containsKey(postId)) {
                        // 搜索关键词高亮替换
                        Post post = idPostMap.get(postId).get(0);
                        String hl_title = highlightDataMap.get(postId).getTitle();
                        String hl_content = highlightDataMap.get(postId).getContent();
                        if (hl_title != null && hl_title.trim() != "") {
                            post.setTitle(hl_title);
                        }
                        if (hl_content != null && hl_content.trim() != "") {
                            post.setContent(hl_content);
                        }
                        resourceList.add(post);
                    } else {
                        // 从 es 清空 db 已物理删除的数据
                        String delete = elasticsearchRestTemplate.delete(String.valueOf(postId), PostEsDTO.class);
                        log.info("delete post {}", delete);
                    }
                });
            }
        }
        page.setRecords(resourceList);
        return page;
    }

    @Override
    public PostVO getPostVO(Post post, HttpServletRequest request) {
        PostVO postVO = PostVO.objToVo(post);
        long postId = post.getId();
        // 1. 关联查询用户信息
        Long userId = post.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("postId", postId);
            postThumbQueryWrapper.eq("userId", loginUser.getId());
            PostThumb postThumb = postThumbMapper.selectOne(postThumbQueryWrapper);
            postVO.setHasThumb(postThumb != null);
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("postId", postId);
            postFavourQueryWrapper.eq("userId", loginUser.getId());
            PostFavour postFavour = postFavourMapper.selectOne(postFavourQueryWrapper);
            postVO.setHasFavour(postFavour != null);
        }
        return postVO;
    }

    @Override
    public Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request) {
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        if (CollectionUtils.isEmpty(postList)) {
            return postVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> postIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> postIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> postIdSet = postList.stream().map(Post::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("postId", postIdSet);
            postThumbQueryWrapper.eq("userId", loginUser.getId());
            List<PostThumb> postPostThumbList = postThumbMapper.selectList(postThumbQueryWrapper);
            postPostThumbList.forEach(postPostThumb -> postIdHasThumbMap.put(postPostThumb.getPostId(), true));
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("postId", postIdSet);
            postFavourQueryWrapper.eq("userId", loginUser.getId());
            List<PostFavour> postFavourList = postFavourMapper.selectList(postFavourQueryWrapper);
            postFavourList.forEach(postFavour -> postIdHasFavourMap.put(postFavour.getPostId(), true));
        }
        // 填充信息
        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVO = PostVO.objToVo(post);
            Long userId = post.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postVO.setUser(userService.getUserVO(user));
            postVO.setHasThumb(postIdHasThumbMap.getOrDefault(post.getId(), false));
            postVO.setHasFavour(postIdHasFavourMap.getOrDefault(post.getId(), false));
            return postVO;
        }).collect(Collectors.toList());
        postVOPage.setRecords(postVOList);
        return postVOPage;
    }

    @Override
    public Page<PostVO> listPostVOByPage(PostQueryRequest postQueryRequest, HttpServletRequest request) {
        long current = postQueryRequest.getCurrent();
        long pageSize = postQueryRequest.getPageSize();
        Page<Post> postPage = this.page(new Page<>(current, pageSize),
                this.getQueryWrapper(postQueryRequest));
        return this.getPostVOPage(postPage, request);
    }

}




