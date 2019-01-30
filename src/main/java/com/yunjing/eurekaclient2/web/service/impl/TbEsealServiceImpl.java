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

import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.Base64;
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
    public TbEseal generate(String creatorID, String creatorType, int type, String userID, String name, String usage,
                            String esID, String pic, int createPicType, String validEnd, String isScene) {

        if(type == TYPE_PERSONAL){
            if((createPicType == PIC_TYPE_OVAL) ||(createPicType == PIC_TYPE_ROUND)){
                throw new RuntimeException("type does not match createPicType");
            }
        }else{
            if(createPicType == PIC_TYPE_PERSONAL){
                throw new RuntimeException("type does not match createPicType");
            }
        }

        //step 1 get signer cert
        TbCertkey tbCertkey=null;
        boolean selfapply = true;
        try {
            switch (creatorType.toUpperCase()) {
                case "OPERATOR":
                    selfapply = false;
                    tbCertkey = checkValidCertExist(userID);
                    if (tbCertkey == null) {
                        // apply cert
                        tbCertkey = applyCert(userID, type, isScene, usage, esID, selfapply);
                    }
                    break;
                default:
                    tbCertkey = checkValidCertExist(creatorID);
                    if (tbCertkey == null) {
                        // apply cert
                        tbCertkey = applyCert(creatorID, type, isScene, usage, esID, selfapply);
                    }
            }

            //
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

        //step 2 get pic
        byte[] picdata= null;
        switch (createPicType){
            case PIC_TYPE_OVAL:
            case PIC_TYPE_ROUND:
                // company pic
                picdata = generateCompanyPic(type,tbCertkey,createPicType);
                break;
            case PIC_TYPE_PERSONAL:
                // personal pic
                picdata = generatePersonalPic(usage);
                break;
                default:
                    picdata = Base64.getUrlDecoder().decode(pic);
        }

        //step 3 generate stamp


    }

    private byte[] generateCompanyPic(int type, TbCertkey tbCertkey, int createPicType) {
        //专用章一律为圆形，中心部位一律为空白，直径为4，0cm，圆边宽为0，1cm，上弧为单位名称，自左而右环行，专用章内容放在章的下边作横排，印文使用简化的宋体字。

        //中外合资（合作），外商独资经营企业的印章
        //
        //规格为椭圆形，横径为4.5cm，竖径为3.0cm，中央不刊五角星（要求刻企业标志可准予），企业名称自左而右环行，或自左而右横排，根椐用章单位的要求，可刻制钢印和中英文印章

        return new byte[0];
    }

    private byte[] generatePersonalPic(String usage) {

        return new byte[0];
    }

    private TbCertkey applyCert(String userID, int type, String isScene, String usage,String esID, boolean selfapply) throws CertificateException, NoSuchProviderException {

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
