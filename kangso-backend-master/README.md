#聚合搜索项目


## 项目简介

基于 Vue 3 + Spring Boot + Elastic Stack 的一站式聚合搜索平台，也是简化版的企业级搜索中台

对用户来说，使用该平台，可以在同一个页面集中搜索出不同来源、不同类型的内容，提升用户的检索效率和搜索体验

对企业来说，当企业内部有多个项目的数据都存在搜索需求时，无需针对每个项目单独开发搜索功能，可以直接将各项目的数据源接入搜索中台，从而提升开发效率、降低系统维护成本。

聚合搜索页面 - 搜文章：

_![](https://yupi-picture-1256524210.cos.ap-shanghai.myqcloud.com/1/1680425753446-db21e8a2-0fd2-496d-8539-b5e3c1f35758.png)_

聚合搜索页面 - 搜图片：

![](https://yupi-picture-1256524210.cos.ap-shanghai.myqcloud.com/1/1680425525242-a6c69abb-1bae-489e-a509-6ddfda0d2c48-20230402170832730.png)

聚合搜索页面 - 搜用户：

![](https://yupi-picture-1256524210.cos.ap-shanghai.myqcloud.com/1/1680425678150-91c35525-a9d4-47e5-9c09-06548c84f7c4-20230402170853604.png)

Elastic Stack - Kibana 数据看板：

![](https://yupi-picture-1256524210.cos.ap-shanghai.myqcloud.com/1/1680425981611-18e62334-1243-4741-9013-124494249fcb.png)



项目架构图：

![](https://yupi-picture-1256524210.cos.ap-shanghai.myqcloud.com/1/image-20230402105911365.png)





### 技术选型（全栈项目）

#### 前端

- Vue 3
- Ant Design Vue 组件库
- 页面状态同步



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

