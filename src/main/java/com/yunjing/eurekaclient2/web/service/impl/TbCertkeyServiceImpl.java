package com.yunjing.eurekaclient2.web.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunjing.eurekaclient2.common.utils.CryptoUtil;
import com.yunjing.eurekaclient2.common.utils.OtherUtil;
import com.yunjing.eurekaclient2.web.entity.TbCertkey;
import com.yunjing.eurekaclient2.web.mapper.TbCertkeyMapper;
import com.yunjing.eurekaclient2.web.service.TbCertkeyService;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 证书和密钥的关联表 服务实现类
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-30
 */
@Service
public class TbCertkeyServiceImpl extends ServiceImpl<TbCertkeyMapper, TbCertkey> implements TbCertkeyService {

    @Override
    public List<TbCertkey> getCertkey(String userID, String IDCard) {
        QueryWrapper<TbCertkey> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userID);
        wrapper.eq("id_card",IDCard);
        wrapper.orderByAsc("end_time");
        //TODO: check order
        List<TbCertkey> list = this.list(wrapper);

        return list;
    }

    @Override
    public TbCertkey insert(String cert, int keyindex, String userID, String IDCard) throws CertificateException, NoSuchProviderException {
        TbCertkey tbCertkey = new TbCertkey();
        tbCertkey.setKeyId(keyindex);
        tbCertkey.setUserId(userID);
        //TODO: base64 or URL base64 parse cert
        byte[] contents = Base64.getUrlDecoder().decode(cert);
        tbCertkey.setCert(new String(contents));
        tbCertkey.setCertHash(CryptoUtil.getDigest(contents));
        CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
        X509Certificate x509Certificate = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(contents));
        BigInteger sn = x509Certificate.getSerialNumber();
        tbCertkey.setCertSn(sn.toString(16));
        Date end = x509Certificate.getNotAfter();
        tbCertkey.setEndTime(OtherUtil.getFromDate(end));
        tbCertkey.setIdCard(IDCard);
        this.save(tbCertkey);
        return tbCertkey;
    }

    @Override
    public int getKeyID(String certKeyList) {
        return 0;
    }


}
