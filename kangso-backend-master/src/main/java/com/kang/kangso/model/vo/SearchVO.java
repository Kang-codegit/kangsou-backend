package com.kang.kangso.model.vo;

import com.kang.kangso.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 聚合搜索
 */
@Data
public class SearchVO implements Serializable {

    private List<UserVO> userList;

    private List<PostVO> postList;

    private List<Picture> pictureList;

    private List<VideoVO> videoList;

    private List<?> dataList;

    private static final long serialVersionUID = 1L;


}
