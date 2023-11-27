package com.kang.kangso;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import top.javatool.canal.client.spring.boot.properties.CanalSimpleProperties;


@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@MapperScan("com.kang.kangso.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MainApplication {

    @Autowired
    private CanalSimpleProperties canalSimpleProperties;

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

}
