package com.yunjing.eurekaclient2.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunjing.eurekaclient2.web.entity.TbEseal;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunjing.eurekaclient2.web.vo.StatisticsVO;

import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;

/**
 * <p>
 * 正在使用的印章数据表 服务类
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-30
 */
public interface TbEsealService extends IService<TbEseal> {

    int TYPE_LEGAL_NAME = 1;
    int TYPE_FINANCE = 2;
    int TYPE_ENVOICE = 3;
    int TYPE_CONTRACT = 4;
    int TYPE_PERSONAL =5;

    int PIC_TYPE_OVAL = 1;
    int PIC_TYPE_ROUND= 2;
    int PIC_TYPE_PERSONAL = 3;

    int STATUS_NORMAL = 0;
    int STATUS_EXPIRE = 1;
    int STATUS_FROZEN = 2;
    int STATUS_REVOKED = 3;
    /** belong to normal*/
    int STATUS_NEED_RENEW = 9;

    //TODO: confirm with user service
     String USER_TYPE_OPERATOR= "OPERATOR";
     String USER_TYPE_PAAS_CLIENT = "PAAS_CLIENT";

    TbEseal generate(String creatorID, String creatorType, int type, String userID, String name, String usage, String esId, String pic, int createPic, String validEnd, String isScene) throws CertificateException, NoSuchProviderException, IOException;

    TbEseal get(int esSN);

    boolean revoke(TbEseal eseal, String comment);

    TbEseal updateEseal(String userID,String userType, int oldEsSN,String validEnd) throws CertificateException, NoSuchProviderException, IOException;

    String getTypeName(Integer type);

    String getStatusName(Integer status);

    StatisticsVO getStatics(String userType, String userID);

    int checkExpire();

    IPage<TbEseal> selectPageVO(Page<TbEseal> pageInfo, Integer type, Integer status, String esId, String name, String userId, LocalDateTime ref);
}
