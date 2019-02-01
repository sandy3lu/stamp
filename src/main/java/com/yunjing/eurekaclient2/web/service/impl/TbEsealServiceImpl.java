package com.yunjing.eurekaclient2.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.yunjing.eurekaclient2.web.vo.StatisticsVO;
import org.apache.http.HttpStatus;
import org.bouncycastle.asn1.*;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Value("${user.define.pic.defaulth}")
    public String defaulth;

    @Value("${user.define.pic.defaultw}")
    public String defaultw;

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

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private TbEseal generatePersonalEsealBySelf(){
        //TODO: Saas平台，个人申请个人章
        return null;
    }

    private TbEseal generatePersonalEseal(String creatorID, String creatorType, int type, String userID, String name, String usage,
            String esID, String pic, int createPicType, String validEnd, String isScene )throws CertificateException, NoSuchProviderException, IOException{

            String cardID=esID;

            byte[] picdata= null;
            if(pic.length()<10) {
                // create pic

                if ((createPicType == PIC_TYPE_OVAL) || (createPicType == PIC_TYPE_ROUND)) {
                    throw new RuntimeException("personal mush create square stamp");
                }
            }

            //step 1 get signer cert
            TbCertkey tbCertkey=null;

            try {
                switch (creatorType.toUpperCase()) {
                    case USER_TYPE_OPERATOR:
                        break;
                    default:
                        userID = creatorID;
                }

                if(isScene.toLowerCase().equals("true")){
                    // apply scene cert for company or personal
                    tbCertkey = applyPersonalCert(userID, isScene, usage, cardID);
                }else{
                    tbCertkey = checkValidPersonalCert(userID, cardID);
                    if (tbCertkey == null) {
                            tbCertkey = applyPersonalCert(userID,  isScene, usage, cardID);
                    }
                }

            }catch (Exception e){
                throw new RuntimeException(e.getMessage());
            }

            //step 2 get pic
            switch (createPicType){
                case PIC_TYPE_PERSONAL:
                    // personal pic
                    picdata = generatePersonalPic(usage);
                    break;
                default:
                    picdata = Base64.getUrlDecoder().decode(pic);
            }

            //step 3 generate stamp
            String picFormat=getImageFormat(picdata);
            int width;
            int height;

            switch (createPicType){
                case PIC_TYPE_PERSONAL:
                    width = Integer.valueOf(recw,10);
                    height = Integer.valueOf(rech,10);
                    break;
                default:
                    width = Integer.valueOf(defaultw,10);
                    height = Integer.valueOf(defaulth,10);
            }
            SESESPictrueInfo picinfo = pictrueInfoBuilder(picdata,picFormat,width,height);

            // get esID from ID card number
            esID = generatePersonalESID(esID);

            SESeal seSeal = generateSeseal(esID,type,name,tbCertkey,picinfo);

            // save to db
            TbEseal tbEseal = new TbEseal();
            tbEseal.setEsId(esID);
            tbEseal.setCreatorId(creatorID);
            // 企业替用户申请，归属在企业用户名下
            tbEseal.setUserId(userID);
            tbEseal.setName(name);
            //个人姓名
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
            //企业为个人用户，只申请一个证书
            tbEseal.setCertKeyList(String.valueOf(i));
            boolean result = this.save(tbEseal);
            if(result) {
                return tbEseal;
            }else{
                throw new RuntimeException("save to database error");
            }

    }

    private TbEseal generateCompanyEseal(String creatorID, String creatorType, int type, String userID, String name, String usage,
                            String esID, String pic, int createPicType, String validEnd, String isScene) throws CertificateException, NoSuchProviderException, IOException {

        byte[] picdata= null;
        if(pic.length()<10) {
            // create pic company stamp
            if (createPicType == PIC_TYPE_PERSONAL) {
                throw new RuntimeException("company can not create square stamp");
            }
        }

        //step 1 get signer cert
        TbCertkey tbCertkey=null;
        boolean selfapply = true;
        try {
            switch (creatorType.toUpperCase()) {
                case USER_TYPE_OPERATOR:
                    selfapply = false;
                    break;
                default:
                    userID = creatorID;
            }
            // TODO:后续扩展为多证书
            if(isScene.toLowerCase().equals("true")){
                // apply scene cert for company or personal
                tbCertkey = applyCompanyCert(userID, isScene, selfapply);
            }else{
                tbCertkey = checkValidCompanyCert(userID);
                if (tbCertkey == null) {
                    tbCertkey = applyCompanyCert(userID,  isScene,  selfapply);
                }
            }

        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

        //step 2 get pic
        switch (createPicType){
            case PIC_TYPE_OVAL:
                picdata = generateCompanyOvalPic(type,tbCertkey);
                break;
            case PIC_TYPE_ROUND:
                // company pic
                picdata = generateCompanyRoundPic(type,tbCertkey);
                break;
            default:
                picdata = Base64.getUrlDecoder().decode(pic);
        }

        //step 3 generate stamp
        String picFormat=getImageFormat(picdata);
        int width;
        int height;

        switch (createPicType){
            case PIC_TYPE_OVAL:
                width = Integer.valueOf(ovalw,10);
                height = Integer.valueOf(ovalw,10);
                break;
            case PIC_TYPE_ROUND:
                width = height = Integer.valueOf(round,10);
                break;

            default:
                width = Integer.valueOf(defaultw,10);
                height = Integer.valueOf(defaulth,10);

        }
        SESESPictrueInfo picinfo = pictrueInfoBuilder(picdata,picFormat,width,height);

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
        //TODO: certKey,后续扩展为多证书
        Integer i = tbCertkey.getId();
        tbEseal.setCertKeyList(String.valueOf(i));
        boolean result = this.save(tbEseal);
        if(result) {
            return tbEseal;
        }else{
            throw new RuntimeException("save to database error");
        }

    }

    @Override
    public TbEseal generate(String creatorID, String creatorType, int type, String userID, String name, String usage,
                            String esID, String pic, int createPicType, String validEnd, String isScene) throws CertificateException, NoSuchProviderException, IOException {


        if(type == TYPE_PERSONAL){
            return generatePersonalEseal(creatorID,creatorType,type,userID,name,usage,esID,pic,createPicType,validEnd,isScene);
        }else{
            return generateCompanyEseal(creatorID,creatorType,type,userID,name,usage,esID,pic,createPicType,validEnd,isScene);
        }

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
            boolean r = this.removeById(eseal.getId());
            if(r){
                return true;
            }else{
                throw new RuntimeException(eseal.getId() +" can not remove from Eseal ");
            }
        }else{
            throw new RuntimeException( eseal.getId() + " can not insert to EsealExpire ");
        }

    }

    @Override
    public TbEseal updateEseal(String userID,String userType,int oldEsSN, String validEnd) throws CertificateException, NoSuchProviderException, IOException {
        TbEseal tbEseal = get(oldEsSN);

        int type = 0;
        String usage = null;
        String certList = null;
        String oldUserID = null;
        if(tbEseal==null){

            TbEsealExpire tbEsealExpire = tbEsealExpireService.get(oldEsSN);
            if(tbEsealExpire == null){
                throw new RuntimeException("could not find " + oldEsSN);
            }
            oldUserID = tbEsealExpire.getUserId();

            type = tbEsealExpire.getType();
            usage = tbEsealExpire.getUsage();
            certList = tbEsealExpire.getCertKeyList();
            // fill
            tbEseal = new TbEseal();
            tbEseal.setUserId(tbEsealExpire.getUserId());
            tbEseal.setContent(tbEsealExpire.getContent());
            tbEseal.setEsId(tbEsealExpire.getEsId());
            tbEseal.setType(type);
            tbEseal.setName(tbEsealExpire.getName());
            tbEseal.setUsage(usage);
        }else{
            oldUserID = tbEseal.getUserId();
            type = tbEseal.getType();
            usage = tbEseal.getUsage();
            certList = tbEseal.getCertKeyList();
        }

        // check authority
        switch (userType){
            case USER_TYPE_OPERATOR:
                break;
            default:
                if(!userID.equals(oldUserID)){
                    throw new RuntimeException(userID + " is not authorized to update " + oldEsSN);
                }
        }

        if(type == TYPE_PERSONAL){
            //企业申请的个人章，里面只会包含一个证书，企业也不会为同一个人管理多个有效的章
            int certid = Integer.valueOf(certList);
            TbCertkey tbCertkey = tbCertkeyService.getCertkey(certid);
            TbCertkey new_tbCertkey=null;
            if(tbCertkey.getIsScene() == 0){
                //判断个人证书的有效期
                LocalDateTime end = tbCertkey.getValidEnd();
                if(end.isBefore(OtherUtil.getReferenceDate())) {
                    new_tbCertkey = applyPersonalCert(tbCertkey.getUserId(), "false", usage, tbCertkey.getIdCard());
                }else{
                    // 有效期还有多于30天, 不申请
                    new_tbCertkey = tbCertkey;
                }
            }else{
                //申请场景证书
                new_tbCertkey = applyPersonalCert(tbCertkey.getUserId(),"true",usage,tbCertkey.getIdCard());
            }

            //替换old stamp中的证书，重新签名
            return updatePersonalEseal(tbEseal,new_tbCertkey,userID,userType,validEnd);
        }else{
            // 个人章与企业章的证书逻辑不同，企业有很多章，有可能别的章更新过了，已经有新的证书了，只是这个章没有，因此不能用old stamp的certlist去查
            // 企业更新自己的章,找到最新的企业证书,不能用userID，这个可能是管理员替企业更新
            TbCertkey  tbCertkey = checkValidCompanyCert(oldUserID);
            if(tbCertkey == null){
                // apply cert
                //TODO: 目前只有一个证书，如果有多个证书，需要制定一个证书策略，用id_card 区分企业章里的不同用户证书
                int certid = Integer.valueOf(certList);
                TbCertkey old_tbCertkey = tbCertkeyService.getCertkey(certid);
                String isScene = "false";
                if(old_tbCertkey.getIsScene() != 0){
                    isScene = "true";
                }
                if(userType.toUpperCase().equals(USER_TYPE_OPERATOR)){
                    tbCertkey = applyCompanyCert(oldUserID,isScene,false);
                }else{
                    tbCertkey = applyCompanyCert(oldUserID,isScene,true);
                }

            }

            //
            return updateCompanyEseal(tbEseal,tbCertkey,userID,userType,validEnd);
        }

    }

    @Override
    public String getTypeName(Integer type) {
        switch (type){
            case TYPE_LEGAL_NAME:
                return "电子法定名称章";
            case TYPE_FINANCE:
                return "电子财务专用章";
            case TYPE_ENVOICE:
                return "电子发票专用章";
            case TYPE_CONTRACT:
                return "电子合同专用章";
            case TYPE_PERSONAL:
                return "个人章";
            default:
                return "其他类型章";
        }
    }

    @Override
    public String getStatusName(Integer status) {
        switch (status){
            case STATUS_NORMAL:
                return "使用中";
            case STATUS_EXPIRE:
                return "已过期";
            case STATUS_FROZEN:
                return "被冻结";
            case STATUS_REVOKED:
                return "被撤销";
            default:
                return "无法识别的状态";
        }
    }

    @Override
    public StatisticsVO getStatics(String userType, String userID) {
        StatisticsVO s = new StatisticsVO();
        int normal=0;
        int expire=0;
        int frozen = 0;
        int revoked = 0;
        int needRenew = 0;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime ref = OtherUtil.getReferenceDate();

        List<TbEseal> list = null;
        switch (userType){
            case USER_TYPE_OPERATOR:
                // get all
                list = this.list();
                break;
            default:
                // get userid's stamp
                QueryWrapper<TbEseal> wrapper = new QueryWrapper<>();
                wrapper.eq("user_id",userID);
                list = this.list(wrapper);
        }

        if((list == null) ||(list.size()<1)){
            return s;
        }
        Iterator<TbEseal> it = list.iterator();
        while(it.hasNext()){
            TbEseal tbEseal = it.next();
            int status = tbEseal.getStatus();
            switch (status){
                case STATUS_NORMAL:
                    LocalDateTime date = tbEseal.getValidEnd();
                    if(date.isBefore(now)){
                        expire++;
                        break;
                    }else{
                        if(date.isBefore(ref)){
                            needRenew++;
                            break;
                        }
                        normal++;
                        break;
                    }
                case STATUS_EXPIRE:
                    expire++;
                    break;
                case STATUS_FROZEN:
                    frozen++;
                    break;
                case STATUS_REVOKED:
                    revoked++;
                    break;
                default:

            }
        }

        s.setExpire(expire);
        s.setUsing(normal + needRenew);
        s.setFrozen(frozen);
        s.setNeedRenew(needRenew);
        s.setRevoked(revoked);
        return s;
    }

    @Override
    public int checkExpire() {
        int count = 0;
        LocalDateTime now = LocalDateTime.now();
            List<TbEseal> list = this.list();

            if((list == null) ||(list.size()<1)){
                return count;
            }
            Iterator<TbEseal> it = list.iterator();
            while(it.hasNext()){
                TbEseal tbEseal = it.next();
                int status = tbEseal.getStatus();
                switch (status){
                    case STATUS_NORMAL:
                        LocalDateTime date = tbEseal.getValidEnd();
                        if(date.isBefore(now)){
                            boolean result = expire(tbEseal);
                            if(result){
                                count++;
                            }
                        }
                        break;
                    case STATUS_EXPIRE:
                        boolean result = expire(tbEseal);
                        if(result){
                            count++;
                        }
                        break;
                    case STATUS_FROZEN:

                        break;
                    case STATUS_REVOKED:
                        revoke(tbEseal,"by checkExpire ");
                        break;
                        default:

                }
            }
        return count;
    }

    @Override
    public IPage<TbEseal> selectPageVO(Page<TbEseal> pageInfo, Integer type, Integer status, String esId, String name, String userId, LocalDateTime ref) {

        return this.baseMapper.selectPageVO(pageInfo,  type,  status,  esId,  name,  userId,ref);
    }

    @Transactional
     boolean expire(TbEseal eseal){

            // change status
            eseal.setStatus(STATUS_EXPIRE);
            boolean result = tbEsealExpireService.insert(eseal);
            if(result){
                boolean r = this.removeById(eseal.getId());
                if(r){
                    return true;
                }else{
                    logger.info(eseal.getId() +" can not remove from Eseal ");
                }
            }else{
                logger.info( eseal.getId() + " can not insert to EsealExpire ");
            }
            return false;
    }

    private TbEseal updatePersonalEseal(TbEseal old_tbEseal,TbCertkey tbCertkey, String creatorID, String creatorType, String validEnd )throws CertificateException, NoSuchProviderException, IOException{


            byte[] data = old_tbEseal.getContent().getBytes();
            SESeal old_seSeal = SESeal.getInstance(data);
            SESeal seSeal = generateSeseal(old_tbEseal.getEsId(),old_tbEseal.getType(),old_tbEseal.getName(),tbCertkey,old_seSeal.getEsealInfo().getPicture());

            // save to db
            TbEseal tbEseal = new TbEseal();
            tbEseal.setEsId(old_tbEseal.getEsId());
            //creator 有可能变更，以前是operator，后来是企业用户，或者反过来
            tbEseal.setCreatorId(creatorID);
            // userid不会变
            tbEseal.setUserId(old_tbEseal.getUserId());
            tbEseal.setName(old_tbEseal.getName());
            //个人姓名
            tbEseal.setUsage(old_tbEseal.getUsage());
            tbEseal.setType(old_tbEseal.getType());
            tbEseal.setStatus(STATUS_NORMAL);
            tbEseal.setComment("");
            try {
                //新日期
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
            //企业为个人用户，只申请一个证书
            tbEseal.setCertKeyList(String.valueOf(i));

        boolean result = this.save(tbEseal);
        if(result){
            // 撤销旧印章
            revoke(old_tbEseal,"renew by " + creatorID);
            return tbEseal;
        }else{
            throw new RuntimeException("save to database error");
        }

    }



    private TbEseal updateCompanyEseal(TbEseal old_tbEseal,TbCertkey tbCertkey, String creatorID, String creatorType,
                                          String validEnd) throws CertificateException, NoSuchProviderException, IOException {

        byte[] data = old_tbEseal.getContent().getBytes();
        SESeal old_seSeal = SESeal.getInstance(data);
        SESeal seSeal = generateSeseal(old_tbEseal.getEsId(),old_tbEseal.getType(),old_tbEseal.getName(),tbCertkey,old_seSeal.getEsealInfo().getPicture());

        // save to db
        TbEseal tbEseal = new TbEseal();
        tbEseal.setEsId(old_tbEseal.getEsId());
        tbEseal.setCreatorId(creatorID);
        tbEseal.setUserId(old_tbEseal.getUserId());
        tbEseal.setName(old_tbEseal.getName());
        tbEseal.setUsage(old_tbEseal.getUsage());
        tbEseal.setType(old_tbEseal.getType());
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
        //TODO: certKey,后续扩展为多证书
        Integer i = tbCertkey.getId();
        tbEseal.setCertKeyList(String.valueOf(i));
        boolean result = this.save(tbEseal);
        if(result){
            // 撤销旧印章
            revoke(old_tbEseal,"renew by " + creatorID);
            return tbEseal;
        }else{
            throw new RuntimeException("save to database error");
        }

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

    private TbCertkey applyCompanyCert(String userID, String isScene,  boolean selfapply) throws CertificateException, NoSuchProviderException {

        CertInfo certInfo = new CertInfo();

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
        if(isScene.toLowerCase().contains("false")){
            certInfo.setType(2);
        }else{
            // scene cert
            certInfo.setType(3);
        }

        String cert_result = certServiceRemote.apply(certInfo,userID);
        net.sf.json.JSONObject cert_jsonObject= net.sf.json.JSONObject.fromObject(cert_result);
        Object cert = cert_jsonObject.get("certData");
        if(cert==null){
            //fail
            throw new RuntimeException(result);
        }

        int keyindex = (int)cert_jsonObject.get("keyId");
        TbCertkey tbCertkey = tbCertkeyService.insert((String)cert, keyindex,userID, "");
        return tbCertkey;
    }

    private TbCertkey applyPersonalCert(String userID, String isScene, String usage,String IDCard) throws CertificateException, NoSuchProviderException {

        CertInfo certInfo = new CertInfo();

        // card number
        certInfo.setId(IDCard);
        // person name
        certInfo.setName(usage);
        certInfo.setEmail("");
        certInfo.setImage("");
        certInfo.setPhoneNumber("");

        if(isScene.toLowerCase().contains("false")) {
            // person cert
            certInfo.setType(1);

            // 已注册用户自己申请证书，目前Paas平台都是企业替用户申请，用户没有注册
            //String result = userServiceRemote.getUserInfo(userID);
            //net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(result);

        }else{
            // scene cert
            certInfo.setType(3);
        }

        String result = certServiceRemote.apply(certInfo,userID);
        net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(result);
        Object cert = jsonObject.get("certData");
        if(cert==null){
            //fail
            throw new RuntimeException(result);
        }

        int keyindex = (int)jsonObject.get("keyId");
        TbCertkey tbCertkey = tbCertkeyService.insert((String)cert, keyindex,userID,IDCard);
        return tbCertkey;
    }



    private TbCertkey checkValidCompanyCert(String applierID){

        List<TbCertkey> list = tbCertkeyService.getCertkey(applierID, "",true);
        if((list == null) || (list.size()<1)){
            return null;
        }else{
            //TODO: make sure this will get the latest
            TbCertkey tbCertkey=list.get(0);
            LocalDateTime end = tbCertkey.getValidEnd();

            if(end.isBefore(OtherUtil.getReferenceDate())){
                // cert is overdue or within 30 days, need apply a new cert
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

    private TbCertkey checkValidPersonalCert(String applierID,String IDCard){

        List<TbCertkey> list = tbCertkeyService.getCertkey(applierID, IDCard,true);
        if((list == null) || (list.size()<1)){
            return null;
        }else{
            //TODO: make sure this will get the latest
            TbCertkey tbCertkey=list.get(0);
            LocalDateTime end = tbCertkey.getValidEnd();
            if(end.isBefore(OtherUtil.getReferenceDate())){
                // cert is overdue or within 30 days, need apply a new cert
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
