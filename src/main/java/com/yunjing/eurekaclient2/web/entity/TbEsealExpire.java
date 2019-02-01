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
@Accessors(chain = true)
public class TbEsealExpire {

    private static final long serialVersionUID = 1L;

    private Integer id;

    /**
     * 电子印章标识
     */
    private String esId;

    private String userId;

    private String creatorId;

    private String name;

    private String usage;

    private Integer type;

    private Integer status;

    private String comment;

    private LocalDateTime createTime;

    private LocalDateTime validEnd;

    private String content;

    private String certKeyList;

    private LocalDateTime updateTime;


    public static TbEsealExpire getInstance(TbEseal tbEseal){
        TbEsealExpire tbEsealExpire = new TbEsealExpire();
        tbEsealExpire.setId(tbEseal.getId());
        tbEsealExpire.setEsId(tbEseal.getEsId());
        tbEsealExpire.setUserId(tbEseal.getUserId());
        tbEsealExpire.setCreatorId(tbEseal.getCreatorId());
        tbEsealExpire.setName(tbEseal.getName());
        tbEsealExpire.setUsage(tbEseal.getUsage());
        tbEsealExpire.setType(tbEseal.getType());
        tbEsealExpire.setStatus(tbEseal.getStatus());
        tbEsealExpire.setComment(tbEseal.getComment());
        tbEsealExpire.setCreateTime(tbEseal.getCreateTime());
        tbEsealExpire.setValidEnd(tbEseal.getValidEnd());
        tbEsealExpire.setContent(tbEseal.getContent());
        tbEsealExpire.setCertKeyList(tbEseal.getCertKeyList());
        return tbEsealExpire;
    }
}
