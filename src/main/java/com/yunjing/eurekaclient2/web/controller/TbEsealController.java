package com.yunjing.eurekaclient2.web.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunjing.eurekaclient2.common.base.ResultInfo;
import com.yunjing.eurekaclient2.common.utils.OtherUtil;
import com.yunjing.eurekaclient2.web.entity.TbCertkey;
import com.yunjing.eurekaclient2.web.entity.TbEseal;
import com.yunjing.eurekaclient2.web.entity.TbEsealExpire;
import com.yunjing.eurekaclient2.web.service.TbCertkeyService;
import com.yunjing.eurekaclient2.web.service.TbEsealExpireService;
import com.yunjing.eurekaclient2.web.service.TbEsealService;
import com.yunjing.eurekaclient2.web.vo.StatisticsVO;
import io.swagger.annotations.ApiOperation;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * <p>
 * 正在使用的印章数据表 前端控制器
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-30
 */
@RestController
@RequestMapping("/v1.0")
public class TbEsealController {

    @Autowired
    TbEsealService tbEsealService;
    @Autowired
    TbCertkeyService tbCertkeyService;
    @Autowired
    TbEsealExpireService tbEsealExpireService;



    @PostMapping("/eseal")
    @ApiOperation("产生印章")
    public ResultInfo generateEseal(@RequestParam("creatorId")String creatorID,@RequestParam("creatorType")String creatorType,
                                    @RequestParam("type")int type,
                                    @RequestParam("userId")String userID,
                                    @RequestParam("name")String name,@RequestParam("usage")String usage,@RequestParam("esId")String esId,
                                    @RequestParam(value = "pic",defaultValue = "")String pic,
                                    @RequestParam(value = "createPicType",defaultValue = "0")int createPicType,
                                    @RequestParam(value ="validEnd",defaultValue = "", required = false)String validEnd,
                                    @RequestParam(value = "isScene",defaultValue = "false",required = false)String isScene){


        try {
            TbEseal eseal = tbEsealService.generate(creatorID, creatorType, type, userID, name, usage, esId, pic, createPicType, validEnd, isScene);
            if(eseal == null){
                return ResultInfo.error("generate eseal error!");
            }
            return ResultInfo.ok().put("esSN", eseal.getId()).put("esId", eseal.getEsId());
        } catch (Exception e) {
            return ResultInfo.error(e.getMessage());
        }

    }

    @GetMapping("/eseal")
    @ApiOperation("查询印章")
    public ResultInfo getEseal(@RequestParam("esSN")int esSN){
        TbEseal eseal = tbEsealService.get(esSN);
        if(eseal!=null){
            int keyid = tbCertkeyService.getKeyID(eseal.getCertKeyList());
            byte[] content = eseal.getContent().getBytes();
            int status = eseal.getStatus();
            if(status == TbEsealService.STATUS_NORMAL){
                // check if valid
                if(eseal.getValidEnd().isBefore(LocalDateTime.now())){
                    status = TbEsealService.STATUS_EXPIRE;
                }
            }
            return ResultInfo.ok().put("content", Base64.getUrlEncoder().encodeToString(content)).put("sealStatus", status).put("keyID", keyid);
        }else{

            TbEsealExpire expire = tbEsealExpireService.get(esSN);
            if(expire!=null){
                int keyid = tbCertkeyService.getKeyID(expire.getCertKeyList());
                byte[] content = expire.getContent().getBytes();
                return ResultInfo.ok().put("content", Base64.getUrlEncoder().encodeToString(content)).put("sealStatus", expire.getStatus()).put("keyID", keyid);
            }else {
                return ResultInfo.error("could not find " + esSN);
            }
        }
    }

    @DeleteMapping("/eseal")
    @ApiOperation("注销印章")
    public ResultInfo revokeEseal(@RequestParam("userId")String userId, @RequestParam("esSN")int esSN,@RequestParam(value = "comment",defaultValue = "", required = false)String comment){
        TbEseal eseal = tbEsealService.get(esSN);
        if(eseal!=null){
            if((eseal.getCreatorId().equals(userId)) || (eseal.getUserId().equals(userId))){
                boolean result = tbEsealService.revoke(eseal,comment);
                if(result){
                    return ResultInfo.ok();
                }else{
                    return ResultInfo.error("revoke failed!");
                }

            }else{
                return ResultInfo.error(userId + " is not authorized");
            }
        }else{
            return ResultInfo.error("could not find " + esSN);
        }
    }


    @PutMapping("/eseal")
    @ApiOperation("更新印章")
    public ResultInfo updateEseal(@RequestParam("userId")String userId, @RequestParam("userType")String userType,@RequestParam("oldEsSN")int oldEsSN, @RequestParam("validEnd")String validEnd){
        try {
            TbEseal eseal = tbEsealService.updateEseal(userId, userType, oldEsSN, validEnd);
            if (eseal == null) {
                return ResultInfo.error("update eseal " + oldEsSN + "error");
            }
            return ResultInfo.ok().put("esSN", eseal.getId()).put("esId", eseal.getEsId());
        }catch (Exception e){
            return ResultInfo.error(e.getMessage());
        }
    }


    @DeleteMapping("/frozen")
    @ApiOperation("冻结/解冻印章")
    public ResultInfo frozeEseal(@RequestParam("userId")String userId, @RequestParam("esSN")int esSN,@RequestParam(value = "comment",defaultValue = "", required = false)String comment){
        TbEseal eseal = tbEsealService.get(esSN);
        if(eseal!=null){
            if((eseal.getCreatorId().equals(userId)) || (eseal.getUserId().equals(userId))){
                boolean result = tbEsealService.revoke(eseal,comment);
                if(result){
                    return ResultInfo.ok();
                }else{
                    return ResultInfo.error("revoke failed!");
                }

            }else{
                return ResultInfo.error(userId + " is not authorized");
            }
        }else{
            return ResultInfo.error("could not find " + esSN);
        }
    }

    @GetMapping("/{userId}/list")
    @ApiOperation("查询印章列表")
    public ResultInfo list(Integer type,Integer status,String esId,String name,
                           @PathVariable("userId")String userId,
                           @RequestParam(value = "offset",defaultValue = "0",required = false) int offset,
                           @RequestParam(value = "limit",defaultValue = "20",required = false) int limit){
        Page<TbEseal> pageInfo = new Page<>(offset, limit);
        IPage<TbEseal> list=null;
        IPage<TbEsealExpire> list_expire = null;
        if(status!=null) {
            switch(status){
                case TbEsealService.STATUS_NORMAL:
                    list = tbEsealService.selectPageVO(pageInfo, type, status, esId, name, userId, null);
                    return ResultInfo.ok().put("normal", list);
                case TbEsealService.STATUS_FROZEN:
                    list = tbEsealService.selectPageVO(pageInfo, type, status, esId, name, userId, null);
                    return ResultInfo.ok().put("frozen", list);
                case TbEsealService.STATUS_REVOKED:
                    list_expire = tbEsealExpireService.selectPageVO(pageInfo,type,status,esId,name,userId);
                    return ResultInfo.ok().put("revoked", list_expire);
                case TbEsealService.STATUS_EXPIRE:
                    list_expire = tbEsealExpireService.selectPageVO(pageInfo,type,status,esId,name,userId);
                    return ResultInfo.ok().put("expire", list_expire);
                case TbEsealService.STATUS_NEED_RENEW:
                    LocalDateTime ref = OtherUtil.getReferenceDate();
                    list = tbEsealService.selectPageVO(pageInfo, type, Integer.valueOf(TbEsealService.STATUS_NORMAL), esId, name, userId, ref);
                    return ResultInfo.ok().put("normal", list);
                    default:
            }

        }

        list = tbEsealService.selectPageVO(pageInfo, type, status, esId, name, userId, null);
        list_expire = tbEsealExpireService.selectPageVO(pageInfo,type,status,esId,name,userId);
        return ResultInfo.ok().put("data", list).put("expire", list_expire);


    }


    @GetMapping("/info/{esSN}")
    @ApiOperation("查询印章详细信息")
    public ResultInfo info(@PathVariable("esSN") int esSN){
        TbEseal eseal = tbEsealService.get(esSN);
        if(eseal == null){
            TbEsealExpire tbEsealExpire = tbEsealExpireService.get(esSN);
            if(tbEsealExpire == null){
                return ResultInfo.error("could not fine " + esSN);
            }else{
                String type = tbEsealService.getTypeName(tbEsealExpire.getType());
                String status = tbEsealService.getStatusName(tbEsealExpire.getStatus());
                String pic = Base64.getUrlEncoder().encodeToString(tbEsealExpire.getContent().getBytes());

                return ResultInfo.ok().put("esID", tbEsealExpire.getEsId()).put("esSN",tbEsealExpire.getId()).put("name",tbEsealExpire.getName()).put("type",type)
                        .put("usage", tbEsealExpire.getUsage()).put("validEnd", tbEsealExpire.getValidEnd().toString()).put("esealStatus", status)
                        .put("createTime", tbEsealExpire.getCreateTime().toString()).put("pic",pic).put("certid", tbEsealExpire.getCertKeyList()).put("userID",tbEsealExpire.getUserId());
            }

        }else{
            String type = tbEsealService.getTypeName(eseal.getType());
            String status = tbEsealService.getStatusName(eseal.getStatus());
            String pic = Base64.getUrlEncoder().encodeToString(eseal.getContent().getBytes());

            return ResultInfo.ok().put("esID", eseal.getEsId()).put("esSN",eseal.getId()).put("name",eseal.getName()).put("type",type)
                    .put("usage", eseal.getUsage()).put("validEnd", eseal.getValidEnd().toString()).put("esealStatus", status)
                    .put("createTime", eseal.getCreateTime().toString()).put("pic",pic).put("certid", eseal.getCertKeyList()).put("userID",eseal.getUserId());
        }


    }

    @GetMapping("/info/{certid}/cert")
    @ApiOperation("获取印章关联的证书(下载PEM文件)")
    public void certinfo(@PathVariable("certid") int certid, HttpServletResponse response){
        TbCertkey tbCertkey = tbCertkeyService.getCertkey(Integer.valueOf(certid));
        byte[] cert = tbCertkey.getCert().getBytes();
        String fileName = certid + ".cer";
        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();

            PemObject key = new PemObject("CERTIFICATE", cert);
            PemWriter wr = new PemWriter(new OutputStreamWriter(bOut));
            wr.writeObject(key);
            wr.close();

            OtherUtil.downloadFile(response,bOut.toByteArray(),fileName);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    @GetMapping("/info/{certid}/publickey")
    @ApiOperation("获取印章关联的公钥")
    public ResultInfo publickkeyinfo(@PathVariable("certid") int certid){
        TbCertkey tbCertkey = tbCertkeyService.getCertkey(Integer.valueOf(certid));
        byte[] cert = tbCertkey.getCert().getBytes();
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
            X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert));
            PublicKey key = x509Certificate.getPublicKey();
            return ResultInfo.ok().put("publickey", key.toString());
        }catch (Exception e){
            return ResultInfo.error(e.getMessage());
        }
    }


    @GetMapping("/statistics/{userType}/{userID}")
    @ApiOperation("查询印章统计信息")
    public ResultInfo statistics(@PathVariable("userType") String userType,@PathVariable("userID") String userID){
        StatisticsVO statisticsVO = tbEsealService.getStatics(userType,userID);
        StatisticsVO statisticsVO_expire = tbEsealExpireService.getStatics(userType,userID);
        statisticsVO.add(statisticsVO_expire);
        return ResultInfo.ok().put("using", statisticsVO.getUsing()).put("frozen", statisticsVO.getFrozen()).put("revoked", statisticsVO.getRevoked())
                .put("renew", statisticsVO.getNeedRenew()).put("expire", statisticsVO.getExpire());

    }

    @PostMapping("/expire")
    @ApiOperation("将过期印章挪到过期库")
    public ResultInfo checkExpire(){
        try {
            int count = tbEsealService.checkExpire();
            return ResultInfo.ok().put("count", count);
        }catch(Exception e){
            return ResultInfo.error(e.getMessage());
        }
    }

}
