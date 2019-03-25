DROP TABLE IF EXISTS `t_logs`;
CREATE TABLE `t_logs` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `action` varchar(100) DEFAULT NULL COMMENT '产生的动作',
  `data` varchar(2000) DEFAULT NULL COMMENT '产生的数据',
  `author_id` int(10) DEFAULT NULL COMMENT '发生人id',
  `ip` varchar(20) DEFAULT NULL COMMENT '日志产生的ip',
  `created` int(10) DEFAULT NULL COMMENT '日志创建时间',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_attach`;
CREATE TABLE `t_attach` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `fname` varchar(100) NOT NULL DEFAULT '',
  `ftype` varchar(50) DEFAULT '',
  `fkey` varchar(100) NOT NULL DEFAULT '',
  `author_id` int(10) DEFAULT NULL,
  `created` int(10) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `t_comments`;

CREATE TABLE `t_comments` (
  `coid` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'comment表主键',
  `cid` int(10) unsigned DEFAULT '0' COMMENT 'post表主键,关联字段',
  `created` int(10) unsigned DEFAULT '0' COMMENT '评论生成时的GMT unix时间戳',
  `author` varchar(200) DEFAULT NULL COMMENT '评论作者',
  `author_id` int(10) unsigned DEFAULT '0' COMMENT '评论所属用户id',
  `owner_id` int(10) unsigned DEFAULT '0' COMMENT '评论所属内容作者id',
  `mail` varchar(200) DEFAULT NULL COMMENT '评论者邮件',
  `url` varchar(200) DEFAULT NULL COMMENT '评论者网址',
  `ip` varchar(64) DEFAULT NULL COMMENT '评论者ip地址',
  `agent` varchar(200) DEFAULT NULL COMMENT '评论者客户端',
  `content` text COMMENT '评论内容',
  `type` varchar(16) DEFAULT 'comment' COMMENT '评论类型',
  `status` varchar(16) DEFAULT 'approved' COMMENT '评论状态',
  `parent` int(10) unsigned DEFAULT '0' COMMENT '父级评论',
  PRIMARY KEY (`coid`),
  KEY `cid` (`cid`),
  KEY `created` (`created`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='评论';

DROP TABLE IF EXISTS `t_contents`;

CREATE TABLE t_contents (
  cid INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL UNIQUE,
  title VARCHAR ( 255 ) NOT NULL,
  slug VARCHAR ( 255 )  UNIQUE,
  thumb_img VARCHAR ( 255 ),
  created INTEGER ( 10 ) NOT NULL,
  modified INTEGER ( 10 ),
  content TEXT,
  author_id INTEGER ( 10 ) NOT NULL,
  type VARCHAR ( 16 ) NOT NULL,
  STATUS VARCHAR ( 16 ) NOT NULL,
  fmt_type VARCHAR ( 16 ) DEFAULT 'markdown',
  tags VARCHAR ( 200 ),
  categories VARCHAR ( 200 ),
  hits INTEGER ( 10 ) DEFAULT 0,
  comments_num INTEGER ( 1 ) DEFAULT 0,
  allow_comment INTEGER ( 1 ) DEFAULT 1,
  allow_ping INTEGER ( 1 ),
  allow_feed INTEGER ( 1 )
);

INSERT INTO t_contents (cid, title, slug, created, modified, content, author_id, type, status, tags, categories, hits, comments_num, allow_comment, allow_ping, allow_feed) VALUES (1, '关于', 'about', 1487853610, 1487872488, '### Hello World
这是我的关于页面
### 当然还有其他
具体你来写点什么吧', 1, 'page', 'publish', NULL, NULL, 0, 0, 1, 1, 1);
INSERT INTO t_contents (cid, title, slug, created, modified, content, author_id, type, status, tags, categories, hits, comments_num, allow_comment, allow_ping, allow_feed) VALUES (2, '第一篇文章', NULL, 1487861184, 1487872798, '## Hello  World.
> 第一篇文章总得写点儿什么?...
----------
<!--more-->
```java
public static void main(String[] args){
    System.out.println(\"Hello Tale.\");
}
```', 1, 'post', 'publish', '', '默认分类', 10, 0, 1, 1, 1);

INSERT INTO t_contents (allow_feed,allow_ping,allow_comment,comments_num,hits,
                        categories,tags,fmt_type,status,type,author_id,content,modified,created,thumb_img,slug,title,cid) VALUES (
  NULL,1,1,0,0,NULL,NULL,'markdown','publish','page',1,'## 友情链接
- :lock: [王爵的技术博客]()
- :lock: [cyang.tech]()
- :lock: [Bakumon''s Blog]()
## 链接须知
> 请确定贵站可以稳定运营
> 原创博客优先，技术类博客优先，设计、视觉类博客优先
> 经常过来访问和评论，眼熟的
备注：默认申请友情链接均为内页（当前页面）
## 基本信息
                网站名称：Tale博客
                网站地址：https://tale.biezhi.me
请在当页通过评论来申请友链，其他地方不予回复
暂时先这样，同时欢迎互换友链，这个页面留言即可。 ^_^
还有，我会不定时对无法访问的网址进行清理，请保证自己的链接长期有效。',1505643888,1505643727,NULL,'links','友情链接',3);


DROP TABLE IF EXISTS `t_metas`;

CREATE TABLE `t_metas` (
  `mid` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '项目主键',
  `name` varchar(200) DEFAULT NULL COMMENT '名称',
  `slug` varchar(200) DEFAULT NULL COMMENT '项目缩略名',
  `type` varchar(32) NOT NULL DEFAULT '' COMMENT '项目类型',
  `description` varchar(200) DEFAULT NULL COMMENT '选项描述',
  `sort` int(10) unsigned DEFAULT '0' COMMENT '项目排序',
  `parent` int(10) unsigned DEFAULT '0',
  PRIMARY KEY (`mid`),
  KEY `slug` (`slug`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='项目';

LOCK TABLES `t_metas` WRITE;

INSERT INTO `t_metas` (`mid`, `name`, `slug`, `type`, `description`, `sort`, `parent`)
VALUES
  (1,'默认分类',NULL,'category',NULL,0,0),
  (6,'王爵的技术博客','http://biezhi.me','link',NULL,0,0);

UNLOCK TABLES;

DROP TABLE IF EXISTS `t_options`;

CREATE TABLE `t_options` (
  `name` varchar(32) NOT NULL DEFAULT '' COMMENT '配置名称',
  `value` varchar(1000) DEFAULT '' COMMENT '配置值',
  `description` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='配置表';

LOCK TABLES `t_options` WRITE;

INSERT INTO `t_options` (`name`, `value`, `description`)
VALUES
  ('site_title','Tale博客系统',''),
  ('social_weibo','',NULL),
  ('social_zhihu','',NULL),
  ('social_github','',NULL),
  ('social_twitter','',NULL),
  ('allow_install','0','是否允许重新安装博客'),
  ('site_theme','default',NULL),
  ('site_keywords','博客系统,Blade框架,Tale',NULL),
  ('site_description','博客系统,Blade框架,Tale',NULL);

UNLOCK TABLES;

DROP TABLE IF EXISTS `t_relationships`;

CREATE TABLE `t_relationships` (
  `cid` int(10) unsigned NOT NULL COMMENT '内容主键',
  `mid` int(10) unsigned NOT NULL COMMENT '项目主键',
  PRIMARY KEY (`cid`,`mid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='关联表';

LOCK TABLES `t_relationships` WRITE;

INSERT INTO `t_relationships` (`cid`, `mid`)
VALUES
  (2,1);

UNLOCK TABLES;

DROP TABLE IF EXISTS `t_users`;

CREATE TABLE `t_users` (
  `uid` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'user表主键',
  `username` varchar(32) DEFAULT NULL COMMENT '用户名称',
  `password` varchar(64) DEFAULT NULL COMMENT '用户密码',
  `email` varchar(200) DEFAULT NULL COMMENT '用户的邮箱',
  `home_url` varchar(200) DEFAULT NULL COMMENT '用户的主页',
  `screen_name` varchar(32) DEFAULT NULL COMMENT '用户显示的名称',
  `created` int(10) unsigned DEFAULT '0' COMMENT '用户注册时的GMT unix时间戳',
  `activated` int(10) unsigned DEFAULT '0' COMMENT '最后活动时间',
  `logged` int(10) unsigned DEFAULT '0' COMMENT '上次登录最后活跃时间',
  `group_name` varchar(16) DEFAULT 'visitor' COMMENT '用户组',
  PRIMARY KEY (`uid`),
  UNIQUE KEY `name` (`username`),
  UNIQUE KEY `mail` (`email`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='用户表';