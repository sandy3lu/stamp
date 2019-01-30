package com.yunjing.eurekaclient2.web.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunjing.eurekaclient2.web.entity.TbCertkey;
import com.yunjing.eurekaclient2.web.mapper.TbCertkeyMapper;
import com.yunjing.eurekaclient2.web.service.TbCertkeyService;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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
    public List<TbCertkey> getCertkey(String userID) {
        QueryWrapper<TbCertkey> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userID);
        wrapper.orderByAsc("end_time");
        //TODO: check order
        List<TbCertkey> list = this.list(wrapper);

        return list;
    }

    @Override
    public TbCertkey insert(String cert, int keyindex, String userID) throws CertificateException, NoSuchProviderException {
        TbCertkey tbCertkey = new TbCertkey();
        tbCertkey.setKeyId(keyindex);
        tbCertkey.setUserId(userID);
        //TODO: base64 or URL base64 parse cert
        byte[] contents = Base64.getUrlDecoder().decode(cert);
        tbCertkey.setCert(new String(contents));
        tbCertkey.setCertHash(getDigest(contents));
        CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
        X509Certificate x509Certificate = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(contents));
        BigInteger sn = x509Certificate.getSerialNumber();
        tbCertkey.setCertSn(sn.toString(16));
        Date end = x509Certificate.getNotAfter();
        Instant instant = end.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        tbCertkey.setEndTime(localDateTime);

        this.save(tbCertkey);
        return tbCertkey;
    }

    private String getDigest(byte[] contents){
        SM3Digest sm3Digest = new SM3Digest();
        sm3Digest.reset();
        sm3Digest.update(contents,0,contents.length);
        byte[] out = new byte[sm3Digest.getDigestSize()];
        sm3Digest.doFinal(out,0);
        return ByteUtils.toHexString(out);
    }
}
