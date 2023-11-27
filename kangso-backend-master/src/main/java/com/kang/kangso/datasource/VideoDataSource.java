package com.kang.kangso.datasource;


import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.rholder.retry.Retryer;
import com.kang.kangso.common.ErrorCode;
import com.kang.kangso.exception.BusinessException;
import com.kang.kangso.model.vo.VideoVO;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 视频数据源
 *
 * @author kang
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Component
public class VideoDataSource implements DataSource{

    @Resource
    private Retryer<String> retryer;

    @Override
    public Page<VideoVO> doSearch(String searchText, long pageNum, long pageSize) {

        String url1 = "https://www.bilibili.com/";
        String url2 = String.format("https://api.bilibili.com/x/web-interface/search/type?search_type=video&keyword=%s",searchText);
        HttpCookie cookie = HttpRequest.get(url1).execute().getCookie("buvid3");

        String body = null;
        try {
            body = retryer.call(() -> HttpRequest.get(url2)
                    .cookie(cookie)
                    .execute().body()); 
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"重试失败");
        }

        Map map = JSONUtil.toBean(body, Map.class);
        Map data = (Map)map.get("data");
        JSONArray videoList = (JSONArray) data.get("result");
        Page<VideoVO> page = new Page<>(pageNum,pageSize);
        List<VideoVO> videoVOList = new ArrayList<>();
        for(Object video:videoList){
            JSONObject tempVideo = (JSONObject)video;
            VideoVO videoVo = new VideoVO();
            videoVo.setUpic(tempVideo.getStr("upic"));
            videoVo.setAuthor(tempVideo.getStr("author"));
            videoVo.setPubdate(tempVideo.getInt("pubdate"));
            videoVo.setArcurl(tempVideo.getStr("arcurl"));
            videoVo.setPic("http:"+tempVideo.getStr("pic"));
            String title = Jsoup.clean(tempVideo.getStr("title"), Whitelist.none());
            String description = Jsoup.clean(tempVideo.getStr("description"), Whitelist.none());
            videoVo.setTitle(title);
            videoVo.setDescription(description);
            videoVOList.add(videoVo);
            if(videoVOList.size()>=pageSize){
                break;
            }
        }
        page.setRecords(videoVOList);
        return page;
    }
}
