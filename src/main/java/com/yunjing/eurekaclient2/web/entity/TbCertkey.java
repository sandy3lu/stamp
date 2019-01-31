package com.yunjing.eurekaclient2.web.entity;

import com.yunjing.eurekaclient2.common.base.BaseEntity;
import java.time.LocalDateTime;
import java.sql.Blob;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 证书和密钥的关联表
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TbCertkey extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 使用证书的企业用户id
     */
    private String userId;

    /**
     * 使用证书的个人用户
     */
    private String idCard;

    /**
     * 证书sn
     */
    private String certSn;

    /**
     * 证书hash
     */
    private String certHash;

    /**
     * 证书数据
     */
    private String cert;

    private int isScene;

    /**
     * 证书关联的密钥id
     */
    private Integer keyId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 证书有效期时间
     */
    private LocalDateTime endTime;
}
