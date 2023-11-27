package com.kang.kangso.model.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片
 *
 * @author kang
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class Picture implements Serializable {

    private String title;

    private String url;

    private static final long serialVersionUID = 1L;

}
