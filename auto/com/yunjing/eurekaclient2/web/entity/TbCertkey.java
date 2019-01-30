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
    private Blob cert;

    /**
     * 证书关联的密钥id
     */
    private Integer keyId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
