package com.kang.kangso.datasource;

import com.kang.kangso.model.enums.SearchTypeEnum;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据源注册器
 *
 * @author kang
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Component
public class DataSourceRegistry {

    @Resource
    private PostDataSource postDataSource;

    @Resource
    private UserDataSource userDataSource;
    
    @Resource
    private VideoDataSource videoDataSource;
    
    @Resource
    private PictureDataSource pictureDataSource;

    private Map<String, DataSource<T>> typeDataSourceMap;

    @PostConstruct
    public void doInit() {
        System.out.println(1);
        typeDataSourceMap = new HashMap() {{
            put(SearchTypeEnum.POST.getValue(), postDataSource);
            put(SearchTypeEnum.USER.getValue(), userDataSource);
            put(SearchTypeEnum.PICTURE.getValue(), pictureDataSource);
            put(SearchTypeEnum.VIDEO.getValue(),videoDataSource);
        }};
    }

    public DataSource getDataSourceByType(String type) {
        if (typeDataSourceMap == null) {
            return null;
        }
        return typeDataSourceMap.get(type);
    }
    public DataSource getDataSource(String type){
        if(type==null){
            return null;
        }
        return typeDataSourceMap.get(type);
    }
}
