CREATE TABLE `tb_eseal`(
`id` int NOT NULL AUTO_INCREMENT COMMENT '自增id（常量编码）' ,
`es_id` varchar(256) not null COMMENT '电子印章标识',
`creator_id` varchar(64) not null COMMENT '申请创建eseal的用户id',
`user_id` varchar(64) not null COMMENT '使用这个章的企业的user_id',
`name` varchar(256) not null COMMENT '电子印章名称',
`usage` varchar(128) not null COMMENT '电子印章用途',
`type` tinyint NOT NULL  COMMENT '印章类型，电子法定名称章01，电子财务专用章02，电子发票专用章03，电子合同专用章04，电子名章05，05是个人章' ,
`status` tinyint NOT NULL  COMMENT '印章状态， 0：有效，1：过期，2：冻结，3：注销' ,
`comment` varchar(256) default null COMMENT '冻结、注销时填写的原因',
`create_time` datetime not null COMMENT '创建时间',
`valid_end` datetime not null COMMENT '有效期',
`content` blob not null COMMENT '印章数据',
`cert_key_list` varchar(256) not null COMMENT '印章关联的密钥列表（支撑多证书）',
`update_time` timestamp not null COMMENT '更新时间',
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='正在使用的印章数据表';

----------------------------------------------------------------------------------------


CREATE TABLE `tb_eseal_expire`(
`id` int NOT NULL COMMENT 'id,从eseal表挪过来的数据' ,
`es_id` varchar(256) not null COMMENT '电子印章标识',
`creator_id` varchar(64) not null COMMENT '申请创建eseal的用户id',
`user_id` varchar(64) not null COMMENT '使用这个章的企业的user_id',
`name` varchar(256) not null COMMENT '电子印章名称',
`usage` varchar(128) not null COMMENT '电子印章用途',
`type` tinyint NOT NULL  COMMENT '印章类型，电子法定名称章01，电子财务专用章02，电子发票专用章03，电子合同专用章04，电子名章05，05是个人章' ,
`status` tinyint NOT NULL  COMMENT '印章状态， 0：有效，1：过期，2：冻结，3：注销' ,
`comment` varchar(256) default null COMMENT '冻结、注销时填写的原因',
`create_time` datetime not null COMMENT '创建时间',
`valid_end` datetime not null COMMENT '有效期',
`content` blob not null COMMENT '印章数据',
`cert_key_list` varchar(256) not null COMMENT '印章关联的密钥列表（支撑多证书）',
`update_time` timestamp not null COMMENT '创建时间',
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='过期和注销的印章数据表';
----------------------------------------------------------------------------------------

CREATE TABLE `tb_certkey`(
`id` int NOT NULL COMMENT 'id,从eseal表挪过来的数据' ,
`user_id` varchar(64) not null COMMENT '使用这个证书的企业的user_id',
`id_card` varchar(64) not null COMMENT '企业替个人用户申请证书时，保存用户的身份证号',
`cert_sn` varchar(256) not null COMMENT '证书sn',
`cert_hash` varchar(256) default null COMMENT '证书hash',
`is_scene` int(1) not null COMMENT '是否为场景证书',
`cert` blob not null COMMENT '证书数据',
`end_time` datetime not null COMMENT '证书有效时间',
`key_id` int not null COMMENT '证书关联的密钥id',
`create_time` timestamp not null COMMENT '创建时间',
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='证书和密钥的关联表';
