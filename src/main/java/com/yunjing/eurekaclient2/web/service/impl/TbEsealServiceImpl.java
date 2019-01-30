package com.yunjing.eurekaclient2.web.service.impl;

import com.yunjing.eurekaclient2.common.base.CertInfo;
import com.yunjing.eurekaclient2.feign.remote.CertServiceRemote;
import com.yunjing.eurekaclient2.feign.remote.UserServiceRemote;
import com.yunjing.eurekaclient2.web.entity.TbCertkey;
import com.yunjing.eurekaclient2.web.entity.TbEseal;
import com.yunjing.eurekaclient2.web.mapper.TbEsealMapper;
import com.yunjing.eurekaclient2.web.service.TbCertkeyService;
import com.yunjing.eurekaclient2.web.service.TbEsealService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 正在使用的印章数据表 服务实现类
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-30
 */
@Service
public class TbEsealServiceImpl extends ServiceImpl<TbEsealMapper, TbEseal> implements TbEsealService {

    @Autowired
    TbCertkeyService tbCertkeyService;

    @Autowired
    CertServiceRemote certServiceRemote;

    @Autowired
    UserServiceRemote userServiceRemote;


    @Override
    public TbEseal generate(String creatorID, String userType, int type, String userID, String name, String usage, String esID, String pic, String createPic, String validEnd, String isScene) {

        TbCertkey tbCertkey=null;
        switch (userType.toUpperCase()){
            case "OPERATOR":
                tbCertkey = checkValidCertExist(userID);
                if(tbCertkey == null){
                    // apply cert
                    tbCertkey = applyCert(userID,type, isScene,usage,esID,false);
                }
                break;
                default:
                    tbCertkey = checkValidCertExist(creatorID);
                    if(tbCertkey == null){
                        // apply cert
                        tbCertkey = applyCert(creatorID,type, isScene, usage,esID,true);
                    }
        }

        //



    }

    private TbCertkey applyCert(String userID, int type, String isScene, String usage,String esID, boolean selfapply) {

        CertInfo certInfo = new CertInfo();

        if(type == TYPE_PERSONAL){
            String result = userServiceRemote.getUserInfo(userID);
            net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(result);

            // card number
            certInfo.setId(esID);
            // person name
            certInfo.setName(usage);
            Object obj = jsonObject.get("email");
            if(obj!=null){
                certInfo.setEmail((String)obj);
            }else {
                certInfo.setEmail("");
            }
            certInfo.setImage("");
            obj = jsonObject.get("phone");
            if(obj!=null){
                certInfo.setPhoneNumber((String)obj);
            }else {
                certInfo.setPhoneNumber("");

            }

            if(isScene.toLowerCase().contains("false")) {
                // person cert
                certInfo.setType(1);
            }else{
                // scene cert
                certInfo.setType(3);
            }


        }else{
            // company cert
            String result = null;
            if(selfapply){
                result = userServiceRemote.getSelfInfo();
            }else{
                result = userServiceRemote.getEnterpriseInfo(userID);
            }

            net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(result);
            Object obj = jsonObject.get("orgCode");
            if(obj!=null){
                certInfo.setId((String)obj);
            }else {
                certInfo.setId("");
            }
             obj = jsonObject.get("company");
            if(obj!=null){
                certInfo.setName((String)obj);
            }else {
                certInfo.setName("");
            }

            certInfo.setEmail("");
            certInfo.setImage("");
            certInfo.setPhoneNumber("");
            certInfo.setType(2);
        }

        String result = certServiceRemote.apply(certInfo,userID);
        net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(result);
        Object cert = jsonObject.get("certData");
        if(cert==null){
            //fail
            throw new RuntimeException(result);
        }

        int keyindex = (int)jsonObject.get("keyId");
        TbCertkey tbCertkey = tbCertkeyService.insert((String)cert, keyindex,userID);
        return tbCertkey;
    }


    private TbCertkey checkValidCertExist(String applierID){

        List<TbCertkey> list = tbCertkeyService.getCertkey(applierID);
        if((list == null) || (list.size()<1)){
            return null;
        }else{
            //TODO: make sure this will get the latest
            TbCertkey tbCertkey=list.get(0);
            LocalDateTime end = tbCertkey.getEndTime();
            if(end.isBefore(LocalDateTime.now())){
                // cert is overdue, need apply a new cert
                return null;
            }else{
                // check cert
                String result = certServiceRemote.validate(tbCertkey.getCertSn(),applierID);
                net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(result);
                Object obj = jsonObject.get("result");
                if(obj!=null){
                    String s = (String)obj;
                    if(s.toLowerCase().contains("true")){
                        return tbCertkey;
                    }
                }
                return null;
            }
        }
    }
}
