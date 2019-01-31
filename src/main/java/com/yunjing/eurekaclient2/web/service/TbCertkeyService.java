package com.yunjing.eurekaclient2.web.service;

import com.yunjing.eurekaclient2.web.entity.TbCertkey;
import com.baomidou.mybatisplus.extension.service.IService;

import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * <p>
 * 证书和密钥的关联表 服务类
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-30
 */
public interface TbCertkeyService extends IService<TbCertkey> {

    List<TbCertkey> getCertkey(String userID, String IDCard);

    TbCertkey insert(String cert, int keyindex, String userID, String IDCard) throws CertificateException, NoSuchProviderException;

    int getKeyID(String certKeyList);
}
