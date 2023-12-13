package com.kang.kangso.datasource;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kang.kangso.common.ErrorCode;
import com.kang.kangso.constant.RedisPrefixConst;
import com.kang.kangso.exception.BusinessException;
import com.kang.kangso.model.entity.Picture;
import com.kang.kangso.model.vo.PictureVO;
import com.kang.kangso.model.vo.SearchVO;
import com.kang.kangso.service.PictureService;
import com.kang.kangso.service.RedisService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 图片服务实现类
 */
@Service
public class PictureDataSource implements DataSource<Picture> {
    @Resource
    private RedisService redisService;
    @Resource
    private PictureService pictureService;
    @Override
    public Page<Picture> doSearch(String searchText, long pageNum, long pageSize) {
        long current = (pageNum - 1) * pageSize;
        Page<Picture> pagePicture=new Page<>();
        String url = String.format("https://cn.bing.com/images/search?q=%s&first=%s", searchText, current);
        String searchPicturePrefix= RedisPrefixConst.SEARCH_PICTURE+searchText;
        List<Object> pictureObjectList = redisService.lRange(searchPicturePrefix, 0, -1);
        if(pictureObjectList.size()>0){
//            pagePicture=(Page<Picture>) pictureObjectList.get(0);
            List<Picture> pictureList = (List<Picture>) pictureObjectList.get(0);
            pagePicture.setRecords(pictureList);
            return pagePicture;
        }
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据获取异常");
        }
        Elements elements = doc.select(".iuscp.isv");
        List<Picture> pictures = new ArrayList<>();
        for (Element element : elements) {
            // 取图片地址（murl）
            String m = element.select(".iusc").get(0).attr("m");
            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
            String murl = (String) map.get("murl");
//            System.out.println(murl);
            // 取标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
//            System.out.println(title);
            Picture picture = new Picture();
            picture.setTitle(title);
            picture.setUrl(murl);
            pictures.add(picture);
            if (pictures.size() >= pageSize) {
                break;
            }
        }
        Page<Picture> picturePage = new Page<>(pageNum, pageSize);
        picturePage.setRecords(pictures);
        redisService.lPush(searchPicturePrefix,pictures,60);
        return picturePage;
    }
}
//         如果有缓存从缓存中拿数据
//        Page<Picture> page=new Page<>();
//        String searchPicturePrefix = RedisPrefixConst.SEARCH_PICTURE + searchText;
//        List<Object> pictureObjectList = redisService.lRange(searchPicturePrefix, 0, -1);
//        PictureVO pictureVO=new PictureVO();
//        if (pictureObjectList.size() > 0){
//            page = (Page<Picture>) pictureObjectList.get(0);
//            return PictureVO.picturePage(page);
//            
//        }
