package com.yunjing.eurekaclient2.web.entity;

import com.yunjing.eurekaclient2.common.base.BaseEntity;
import java.time.LocalDateTime;
import java.sql.Blob;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 过期和注销的印章数据表
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TbEsealExpire extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 电子印章标识
     */
    private String esId;


    private String userId;


    private String creatorId;

    /**
     * 电子印章名称
     */
    private String name;

    /**
     * 电子印章用途
     */
    private String usage;

    /**
     * 印章类型，电子法定名称章01，电子财务专用章02，电子发票专用章03，电子合同专用章04，电子名章05，05是个人章
     */
    private Integer type;

    /**
     * 印章状态， 0：有效，1：过期，2：冻结，3：注销
     */
    private Integer status;

    /**
     * 冻结、注销时填写的原因
     */
    private String comment;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 有效期
     */
    private LocalDateTime validEnd;

    /**
     * 印章数据
     */
    private Blob content;

    /**
     * 印章关联的密钥列表（支撑多证书）
     */
    private String certKeyList;

    /**
     * 创建时间
     */
    private LocalDateTime updateTime;


}
