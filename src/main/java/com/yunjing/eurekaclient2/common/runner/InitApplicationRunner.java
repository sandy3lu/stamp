package com.yunjing.eurekaclient2.common.runner;

import com.yunjing.eurekaclient2.common.base.CertInfo;
import com.yunjing.eurekaclient2.feign.remote.CertServiceRemote;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import net.sf.json.JSONObject;

import java.io.FileOutputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * @ClassName InitApplicationRunner
 * @Description 系统启动初始化操作
 * @Author scyking
 * @Date 2019/1/23 16:32
 * @Version 1.0
 */
@Component
public class InitApplicationRunner implements ApplicationRunner {

    protected Logger logger = LoggerFactory.getLogger(getClass());


    @Value("${user.define.crypto.makerkeyfile}")
    public String makerkeyfile;

    @Value("${user.define.cert.name}")
    public String name;
    @Value("${user.define.cert.id}")
    public String id;
    @Value("${user.define.cert.phone}")
    public String phone;
    @Value("${user.define.cert.email}")
    public String email;
    @Value("${user.define.cert.image}")
    public String image;
    @Value("${user.define.cert.type}")
    public String type;
    @Value("${user.define.cert.userId}")
    public String userId;

    @Autowired
    CertServiceRemote certServiceRemote;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("init...");
        Security.addProvider(new BouncyCastleProvider());

        File f = new File(makerkeyfile);

        if (f.exists()) {
            //load into buffer to check file
            logger.info("load key and cert ...");
            FileInputStream in = new FileInputStream(makerkeyfile);
            byte[] buffer = new byte[4096];
            int length = in.read(buffer);
            byte[] tmp = Arrays.copyOfRange(buffer,0,length);
            String result= new String (tmp);
            net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(result);
            Object obj = jsonObject.get("certData");
            if(obj!= null){
                // check result fields
                String certdata = (String)obj;
                int keyindex = (int)jsonObject.get("keyId");

            }else{
                logger.info("read cert file error : " + makerkeyfile);
                return;
            }
            logger.info("init end !");
            return;
        }

        // the first time , generate key
        logger.info("generate key and cert ...");

        //save to file
        X509Certificate cert = null;
        try {
            CertInfo certInfo = new CertInfo();
            certInfo.setId(id);
            certInfo.setName(name);
            certInfo.setEmail(email);
            certInfo.setImage(image);
            certInfo.setPhoneNumber(phone);
            certInfo.setType(Integer.getInteger(type));

            String result = certServiceRemote.apply(certInfo,userId);
            net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(result);
            Object obj = jsonObject.get("certData");
            if(obj!= null){
                 // save to file
                FileOutputStream out = new FileOutputStream(makerkeyfile);
                out.write(result.getBytes());
                out.close();
            }else{
                logger.info("generate cert error : " + result);
                return;
            }


        } catch (Exception e) {
            logger.info("generate cert error : " + e.getMessage());
            return;
        }

        logger.info("init end !");
    }



}
