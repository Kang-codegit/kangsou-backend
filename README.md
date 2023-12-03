# kangsou-backend
##聚合搜索后端
基于 Spring Boot + Elastic Stack (+ Vue 3) 的一站式XX 信息聚合搜索平台
用户可在同一页面集中搜索出文章、图片和b站视频，提升搜索体验


#### 后端

- Spring Boot 2.7 框架 + springboot-init 脚手架
- MySQL 数据库（8.x 版本）
- Elastic Stack
    - Elasticsearch 搜索引擎（重点）
    - Logstash 数据管道
    - Kibana 数据可视化
- 数据抓取（jsoup、HttpClient 爬虫）
    - 离线
    - 实时
- 设计模式
    - 门面模式
    - 适配器模式
    - 注册器模式
- 数据同步（4 种同步方式）
    - 定时
    - 双写
    - Logstash
    - Canal
- JMeter 压力测试
