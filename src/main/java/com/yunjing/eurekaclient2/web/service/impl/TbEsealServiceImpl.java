package com.yunjing.eurekaclient2.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yunjing.eurekaclient2.common.asn1.*;
import com.yunjing.eurekaclient2.common.base.CertInfo;
import com.yunjing.eurekaclient2.common.base.ResultInfo;
import com.yunjing.eurekaclient2.common.picGenerate.ImgGen;
import com.yunjing.eurekaclient2.common.picGenerate.SealImgGen;
import com.yunjing.eurekaclient2.common.utils.CryptoUtil;
import com.yunjing.eurekaclient2.common.utils.OtherUtil;
import com.yunjing.eurekaclient2.feign.remote.CertServiceRemote;
import com.yunjing.eurekaclient2.feign.remote.KeyServiceRemote;
import com.yunjing.eurekaclient2.feign.remote.SignServiceRemote;
import com.yunjing.eurekaclient2.feign.remote.UserServiceRemote;
import com.yunjing.eurekaclient2.web.entity.TbCertkey;
import com.yunjing.eurekaclient2.web.entity.TbEseal;
import com.yunjing.eurekaclient2.web.entity.TbEsealExpire;
import com.yunjing.eurekaclient2.web.mapper.TbEsealMapper;
import com.yunjing.eurekaclient2.web.service.TbCertkeyService;
import com.yunjing.eurekaclient2.web.service.TbEsealExpireService;
import com.yunjing.eurekaclient2.web.service.TbEsealService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.http.HttpStatus;
import org.bouncycastle.asn1.*;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
    TbEsealExpireService tbEsealExpireService;

    @Autowired
    CertServiceRemote certServiceRemote;

    @Autowired
    UserServiceRemote userServiceRemote;

    @Autowired
    SignServiceRemote signServiceRemote;

    @Autowired
    KeyServiceRemote keyServiceRemote;

    @Value("${user.define.pic.round}")
    public String round;

    @Value("${user.define.pic.ovalw}")
    public String ovalw;

    @Value("${user.define.pic.ovalh}")
    public String ovalh;

    @Value("${user.define.pic.recw}")
    public String recw;

    @Value("${user.define.pic.rech}")
    public String rech;

    @Value("${user.define.esid.prefix}")
    public String prefix;

    @Value("${user.define.crypto.makerkeyfile}")
    public String makerkeyfile;

    @Value("${user.define.cert.userId}")
    public String userId;


    private static BigInteger VERSION =  BigInteger.valueOf(4);
    private static ASN1ObjectIdentifier SM2signatureAlgorithm = new ASN1ObjectIdentifier("1.2.156.10197.1.501");
    private static DERIA5String VID = new DERIA5String("yunjingit.com");
    private static ASN1Integer CERT_LIST_TYPE_HASH = new ASN1Integer(2);
    private static ASN1Integer CERT_LIST_TYPE_ALL = new ASN1Integer(1);
    private static DERPrintableString OBJTYPE = new DERPrintableString("certificate SM3 digest");

    private static ASN1ObjectIdentifier ID_SEALMAKINGUNITINFO = new ASN1ObjectIdentifier("1.2.156.112600.7.1");
    private static ASN1ObjectIdentifier ID_SEALHOLDINGUNIT_ETHNICMINORITIESNAME = new ASN1ObjectIdentifier("1.2.156.112600.7.2");
    private static ASN1ObjectIdentifier ID_SEALHOLDINGUNIT_ENGLISHNAME = new ASN1ObjectIdentifier("1.2.156.112600.7.3");
    private static ASN1OctetString MAKINGUNITINFO = new DEROctetString("91110105061311021A 北京云京科技有限公司".getBytes());
    private static String makerPrivKey;
    private static String makerPubKey;
    private static byte[] makerCert;


    @Override
    public TbEseal generate(String creatorID, String creatorType, int type, String userID, String name, String usage,
                            String esID, String pic, int createPicType, String validEnd, String isScene) throws CertificateException, NoSuchProviderException, IOException {

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
                    break;
                default:
                    userID = creatorID;
            }
            tbCertkey = checkValidCertExist(userID);
            if (tbCertkey == null) {
                // apply cert
                tbCertkey = applyCert(userID, type, isScene, usage, esID, selfapply);
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

        //step 2 get pic
        byte[] picdata= null;
        switch (createPicType){
            case PIC_TYPE_OVAL:
                picdata = generateCompanyOvalPic(type,tbCertkey);
                break;
            case PIC_TYPE_ROUND:
                // company pic
                picdata = generateCompanyRoundPic(type,tbCertkey);
                break;
            case PIC_TYPE_PERSONAL:
                // personal pic
                picdata = generatePersonalPic(usage);
                break;
                default:
                    picdata = Base64.getUrlDecoder().decode(pic);
        }

        //step 3 generate stamp
        String picFormat=getImageFormat(picdata);
        int width=20;
        int height=20;

        switch (createPicType){
            case PIC_TYPE_OVAL:
                width = Integer.valueOf(ovalw,10);
                height = Integer.valueOf(ovalw,10);
                break;
            case PIC_TYPE_ROUND:
                width = height = Integer.valueOf(round,10);
                break;
            case PIC_TYPE_PERSONAL:
                width = Integer.valueOf(recw,10);
                height = Integer.valueOf(rech,10);
                break;
                default:
                    width = Integer.valueOf(recw,10);
                    height = Integer.valueOf(rech,10);

        }
        SESESPictrueInfo picinfo = pictrueInfoBuilder(picdata,picFormat,width,height);

        if(type == TYPE_PERSONAL){
            // get esID from ID card number
            esID = generatePersonalESID(esID);
        }
        SESeal seSeal = generateSeseal(esID,type,name,tbCertkey,picinfo);

        // save to db
        TbEseal tbEseal = new TbEseal();
        tbEseal.setEsId(esID);
        tbEseal.setCreatorId(creatorID);
        tbEseal.setUserId(userID);
        tbEseal.setName(name);
        tbEseal.setUsage(usage);
        tbEseal.setType(type);
        tbEseal.setStatus(STATUS_NORMAL);
        tbEseal.setComment("");
        try {
            Date create = seSeal.getEsealInfo().getProperty().getCreateDate().getDate();
            tbEseal.setCreateTime(OtherUtil.getFromDate(create));
            Date end = seSeal.getEsealInfo().getProperty().getValidEnd().getDate();
            tbEseal.setValidEnd(OtherUtil.getFromDate(end));
        }catch(Exception e){
            throw new RuntimeException("SESeal parse error: " + e.getMessage());
        }
        tbEseal.setContent(new String(seSeal.getEncoded()));
        // certKey
        Integer i = tbCertkey.getId();
        tbEseal.setCertKeyList(String.valueOf(i));
        this.save(tbEseal);
        return tbEseal;
    }

    @Override
    public TbEseal get(int esSN) {
        QueryWrapper<TbEseal> wrapper = new QueryWrapper<>();
        wrapper.eq("id",esSN);
        TbEseal key = this.getOne(wrapper);
        return key;
    }

    @Override
    @Transactional
    public boolean revoke(TbEseal eseal, String comment) {
        // change status
        eseal.setStatus(STATUS_REVOKED);
        eseal.setComment(comment);
        boolean result = tbEsealExpireService.insert(eseal);

        if(result){
            return this.removeById(eseal.getId());
        }
        return false;
    }

    @Override
    public TbEseal updateEseal(int oldEsSN) {
        TbEseal tbEseal = get(oldEsSN);
        String userID = null;
        int type = 0;
        String usage = null;
        if(tbEseal==null){

            TbEsealExpire tbEsealExpire = tbEsealExpireService.get(oldEsSN);
            if(tbEsealExpire == null){
                throw new RuntimeException("could not find " + oldEsSN);
            }
            userID = tbEsealExpire.getUserId();
            type = tbEsealExpire.getType();
            usage = tbEsealExpire.getUsage();
        }else{
            userID = tbEseal.getUserId();
            type = tbEseal.getType();
            usage = tbEseal.getUsage();

        }

        TbCertkey  tbCertkey = checkValidCertExist(userID);
        LocalDateTime end = tbCertkey.getEndTime();
        LocalDateTime ref = OtherUtil.plus(LocalDateTime.now(),31, ChronoUnit.DAYS);
        if(end.isBefore(ref)){
            // apply cert

            TbCertkey tbCertkey1 = applyCert(userID, type, String isScene, usage,String esID, boolean selfapply)

        }


        return null;
    }

    private String generatePersonalESID(String esID) {
        // esID is personal card number
        Date date = new Date();
        //the number of milliseconds since January 1, 1970, 00:00:00 GMT
        long time = date.getTime();
        String stime = String.valueOf(time);
        String digest = CryptoUtil.getDigest(esID.getBytes());
        return prefix + digest.substring(0,4) + stime;
    }

    private byte[] generateCompanyOvalPic(int type, TbCertkey tbCertkey) throws CertificateException, NoSuchProviderException {
         //中外合资（合作），外商独资经营企业的印章
        //
        //规格为椭圆形，横径为4.5cm，竖径为3.0cm，中央不刊五角星（要求刻企业标志可准予），企业名称自左而右环行，或自左而右横排，根椐用章单位的要求，可刻制钢印和中英文印章
        SealImgGen sealImgGen = new SealImgGen();
        String foot = getPicFoot(type);
        String name = getCompanyNamefromCert(tbCertkey);
        return sealImgGen.genOvalSeal(name,foot,ImgGen.centerValue);

    }

    private byte[] generateCompanyRoundPic(int type, TbCertkey tbCertkey) throws CertificateException, NoSuchProviderException {
        //专用章一律为圆形，中心部位一律为空白，直径为4，0cm，圆边宽为0，1cm，上弧为单位名称，自左而右环行，专用章内容放在章的下边作横排，印文使用简化的宋体字。
        ImgGen imgGen = new ImgGen();
        String foot = getPicFoot(type);
        String name = getCompanyNamefromCert(tbCertkey);
        return imgGen.genCircleSeal(name,foot,ImgGen.centerValue);

    }

    private String getCompanyNamefromCert(TbCertkey tbCertkey) throws CertificateException, NoSuchProviderException {
        byte[] contents = tbCertkey.getCert().getBytes();
        CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
        X509Certificate x509Certificate = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(contents));
        String name = x509Certificate.getSubjectX500Principal().getName();
        return name;
    }

    public String getPicFoot(int type){
        switch (type){
            case TYPE_ENVOICE:
                return "发票专用章";
            case TYPE_CONTRACT:
                return "合同专用章";
            case TYPE_FINANCE:
                return "财务专用章";
            case TYPE_LEGAL_NAME:
                return " ";
                default: return " ";
        }
    }
    private byte[] generatePersonalPic(String usage) {
        //法人名章一般2*2 财务会计1.8*1.8或更小,其余方形印章不能超过2*2,个人方章建议1.8*1.8合适
        ImgGen imgGen = new ImgGen();
        return imgGen.genRectangleSeal(usage);
    }

    private TbCertkey applyCert(String userID, int type, String isScene, String usage,String IDCard, boolean selfapply) throws CertificateException, NoSuchProviderException {

        CertInfo certInfo = new CertInfo();

        if(type == TYPE_PERSONAL){

            if(isScene.toLowerCase().contains("false")) {
                // person cert
                certInfo.setType(1);
                String result = userServiceRemote.getUserInfo(userID);
                net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(result);

                // card number
                certInfo.setId(IDCard);
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
            }else{
                // scene cert
                certInfo.setType(3);
                // card number
                certInfo.setId(IDCard);
                // person name
                certInfo.setName(usage);
                certInfo.setEmail("");

                certInfo.setImage("");
                certInfo.setPhoneNumber("");
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

    private  SESeal parseEseal(byte[] stampdata){
        ASN1StreamParser aIn = new ASN1StreamParser(stampdata);
        ASN1SequenceParser seq = null;
        try {
            seq = (ASN1SequenceParser)aIn.readObject();
            SESeal stamp =  SESeal.getInstance(seq);
            return stamp;
        } catch (Exception e) {
            throw new RuntimeException("eseal format error!");
        }

    }

    public  SESeal generateSeseal(String esID, int type, String name, TbCertkey tbCertkey, SESESPictrueInfo pic) throws CertificateException, NoSuchProviderException, IOException {
        //header
        ASN1Integer version = new ASN1Integer(VERSION);
        SESHeader header = new SESHeader(version,VID);
        //esID
        DERIA5String esID_ia5 = new DERIA5String(esID);
        //property
        ASN1Integer type_asn1 = new ASN1Integer(type);
        DERUTF8String name_asn1 = new DERUTF8String(name);

        //certlist
        ASN1EncodableVector vec = new ASN1EncodableVector();
        byte[] contents = tbCertkey.getCert().getBytes();

        String digest = CryptoUtil.getDigest(contents);
        DEROctetString cert = new DEROctetString(ByteUtils.fromHexString(digest));

        CertDigestObj certDigestObj = new CertDigestObj(OBJTYPE,cert);
        vec.add(certDigestObj);

        DERSequence cert_seq = new DERSequence(vec);

        CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
        X509Certificate x509Certificate = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(contents));
        // stamp validate == cert validate
        DERUTCTime start_asn1 = new DERUTCTime(x509Certificate.getNotBefore());
        DERUTCTime end_asn1 = new DERUTCTime(x509Certificate.getNotAfter());
        DERUTCTime create_asn1 = new DERUTCTime(new Date());
        SESESPropertyInfo propertyInfo = new SESESPropertyInfo(type_asn1,name_asn1,CERT_LIST_TYPE_HASH,cert_seq,create_asn1,start_asn1,end_asn1);

        //extDatas
        ExtData extension = new ExtData(ID_SEALMAKINGUNITINFO,MAKINGUNITINFO);
        ExtensionDatas extensionDatas = new ExtensionDatas(extension);

        //
        SESSealInfo esealInfo = new SESSealInfo(header,esID_ia5,propertyInfo,pic, extensionDatas);

        //cert + signAlgID + signedValue
        if((makerCert==null) ){
            //load from file
            boolean result = loadMakerCert();
            if(!result){
                throw new RuntimeException("some error occurs when loadMakerCert");
            }
        }


        try {
            DEROctetString makercert = new DEROctetString(makerCert);
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(esealInfo);
            v.add(makercert);
            v.add(SM2signatureAlgorithm);
            DERSequence se =  new DERSequence(v);
            byte[] msg = se.getEncoded();
            // call sign service
            String msgBase64 = Base64.getUrlEncoder().encodeToString(msg);
            ResultInfo rs = signServiceRemote.signWithKey(msgBase64,"SM2",makerPrivKey,makerPubKey);
            int code = (int)rs.get("code");
            if(code == HttpStatus.SC_OK){
                String signedBase64 = (String)rs.get("data");
                byte[] sig = Base64.getUrlDecoder().decode(signedBase64);
                DERBitString signData = new DERBitString(sig);
                SESSignInfo signInfo = new SESSignInfo(cert,SM2signatureAlgorithm, signData);

                return new SESeal(esealInfo,signInfo);
            }else{
                // could not get key from key service remote
                throw new RuntimeException("sign service error: " + rs.get("msg"));
            }


        } catch (Exception  e) {
            throw new RuntimeException("sign eseal error : " + e.getMessage());
        }

    }

    private  boolean loadMakerCert() throws IOException {

        File f = new File(makerkeyfile);

        if (f.exists()) {

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
                //TODO: check with cert service
                makerCert = Base64.getUrlDecoder().decode(certdata);
                ResultInfo rs = keyServiceRemote.getKey(userId,keyindex);
                int code = (int)rs.get("code");
                if(code == HttpStatus.SC_OK){
                    String publicKey = (String)rs.get("publicKey");
                    String privateKey = (String)rs.get("privateKey");
                    String type = (String)rs.get("keyType");
                    if(type.toLowerCase().contains("sm2")){
                        makerPubKey = publicKey;
                        makerPrivKey = privateKey;
                        return true;
                    }
                }else{
                    // could not get key from key service remote
                    throw new RuntimeException("key service error: " + rs.get("msg"));
                }


            }else{
                throw new RuntimeException(makerkeyfile + " is wrong ");

            }

        }else{
            throw new RuntimeException(makerkeyfile + " is missing ");
        }
        return false;
    }

    private   static SESESPictrueInfo pictrueInfoBuilder(byte[] imgdata, String picType, int widthinmm, int heightinmm){

        DERIA5String type = new DERIA5String(picType);
        ASN1OctetString data = new DEROctetString(imgdata);
        ASN1Integer width = new ASN1Integer(widthinmm);
        ASN1Integer height = new ASN1Integer(heightinmm);

        SESESPictrueInfo sesesPictrueInfo = new SESESPictrueInfo(type,data,width,height);
        return sesesPictrueInfo;
    }

    private static String getImageFormat(byte[] imageData) {
        ByteArrayInputStream bs = new ByteArrayInputStream(imageData);
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(bs);
            Iterator<ImageReader> iterator = ImageIO.getImageReaders(iis);
            while (iterator.hasNext()) {
                ImageReader reader =  iterator.next();
                //JPEG、GIF
                return reader.getFormatName();
            }
            System.out.println("cound not find image format!");
            return "PNG";
        } catch (Exception e) {
            e.printStackTrace();
            return "PNG";
        }

    }






}
