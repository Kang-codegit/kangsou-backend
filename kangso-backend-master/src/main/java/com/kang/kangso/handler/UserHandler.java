package com.kang.kangso.handler;


import com.kang.kangso.esdao.PostEsDao;
import com.kang.kangso.model.dto.post.PostEsDTO;
import com.kang.kangso.model.entity.Post;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;


import javax.annotation.Resource;


@CanalTable("post")
@Component
public class UserHandler implements EntryHandler<Post> {

    @Resource
    private PostEsDao postEsDao;

    @Override
    public void insert(Post post) {
        postEsDao.save(PostEsDTO.objToDto(post));
        System.out.println("新增用户");
        System.out.println("user = " + post);
    }
 
    @Override
    public void update(Post before, Post after) {
        System.out.println("修改用户");
        System.out.println("修改用户before：" + before);
        System.out.println("修改用户after：" + after);
        postEsDao.save(PostEsDTO.objToDto(after));
    }
 
    @Override
    public void delete(Post post) {
        postEsDao.delete(PostEsDTO.objToDto(post));
        System.out.println("删除用户user = " + post);
    }
 
}
