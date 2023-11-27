package com.kang.kangso.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kang.kangso.model.entity.Picture;

/**
 * 图片服务
 *
 * @author kang
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface PictureService {

    Page<Picture> searchPicture(String searchText, long pageNum, long pageSize);
}
