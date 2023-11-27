# 建表脚本
# @author kang
# @from <a href="https://yupi.icu">编程导航知识星球</a>

-- 创建库
create database if not exists kang_so;

-- 切换库
use kang_so;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 帖子表
use kang_so;
drop table post;
create table if not exists post
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    href       varchar(1024)                      null comment '链接',
    murl       varchar(1024)                      null comment '图片',
    searchText varchar(1024)                      null comment '搜索词',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '帖子' collate = utf8mb4_unicode_ci;

-- 帖子点赞表（硬删除）
create table if not exists post_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子点赞';

-- https://t.zsxq.com/0emozsIJh

-- 帖子收藏表（硬删除）
create table if not exists post_favour
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子收藏';
-- 插入用户数据
INSERT INTO user (userAccount, userPassword, unionId, mpOpenId, userName, userAvatar, userProfile, userRole, createTime,
                  updateTime, isDelete)
VALUES ('user1@example.com', 'password123', 'wx123456', 'mpOpenId123', 'User One', 'avatar1.jpg', 'This is user one.',
        'user', NOW(), NOW(), 0),
       ('user2@example.com', 'password456', 'wx789012', 'mpOpenId456', 'User Two', 'avatar2.jpg', 'This is user two.',
        'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user3@example.com', 'password789', 'wx345678', 'mpOpenId789', 'User Three', 'avatar3.jpg',
        'This is user three.', 'user', NOW(), NOW(), 0),
       ('user4@example.com', 'passwordabc', 'wx901234', 'mpOpenIdabc', 'User Four', 'avatar4.jpg', 'This is user four.',
        'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user5@example.com', 'passworddef', 'wx567890', 'mpOpenIddef', 'User Five', 'avatar5.jpg', 'This is user five.',
        'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user6@example.com', 'passwordghi', 'wx123abc', 'mpOpenIdghi', 'User Six', 'avatar6.jpg', 'This is user six.',
        'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user7@example.com', 'passwordjkl', 'wx789def', 'mpOpenIdjkl', 'User Seven', 'avatar7.jpg',
        'This is user seven.', 'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user8@example.com', 'passwordmno', 'wx345ghi', 'mpOpenIdmno', 'User Eight', 'avatar8.jpg',
        'This is user eight.', 'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user9@example.com', 'passwordpqr', 'wx901jkl', 'mpOpenIdpqr', 'User Nine', 'avatar9.jpg', 'This is user nine.',
        'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user10@example.com', 'passwordstu', 'wx567mno', 'mpOpenIdstu', 'User Ten', 'avatar10.jpg', 'This is user ten.',
        'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user11@example.com', 'passwordvwx', 'wx123pqr', 'mpOpenIdvwx', 'User Eleven', 'avatar11.jpg',
        'This is user eleven.', 'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user12@example.com', 'passwordyz1', 'wx789stu', 'mpOpenIdyz1', 'User Twelve', 'avatar12.jpg',
        'This is user twelve.', 'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user13@example.com', 'password234', 'wx345vwx', 'mpOpenId234', 'User Thirteen', 'avatar13.jpg',
        'This is user thirteen.', 'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user14@example.com', 'password567', 'wx901yz1', 'mpOpenId567', 'User Fourteen', 'avatar14.jpg',
        'This is user fourteen.', 'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user15@example.com', 'password890', 'wx567234', 'mpOpenId890', 'User Fifteen', 'avatar15.jpg',
        'This is user fifteen.', 'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user16@example.com', 'passwordabc1', 'wx123567', 'mpOpenIdabc1', 'User Sixteen', 'avatar16.jpg',
        'This is user sixteen.', 'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user17@example.com', 'passworddef1', 'wx789901', 'mpOpenIddef1', 'User Seventeen', 'avatar17.jpg',
        'This is user seventeen.', 'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user18@example.com', 'passwordghi1', 'wx345abc', 'mpOpenIdghi1', 'User Eighteen', 'avatar18.jpg',
        'This is user eighteen.', 'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user19@example.com', 'passwordjkl1', 'wx901def', 'mpOpenIdjkl1', 'User Nineteen', 'avatar19.jpg',
        'This is user nineteen.', 'user', NOW(), NOW(), 0),
-- Add more users here...
       ('user20@example.com', 'passwordmno1', 'wx567ghi', 'mpOpenIdmno1', 'User Twenty', 'avatar20.jpg',
        'This is user twenty.', 'user', NOW(), NOW(), 0);
