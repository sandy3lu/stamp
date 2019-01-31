package com.yunjing.eurekaclient2.web.controller;


import com.yunjing.eurekaclient2.common.base.ResultInfo;
import com.yunjing.eurekaclient2.web.entity.TbEseal;
import com.yunjing.eurekaclient2.web.entity.TbEsealExpire;
import com.yunjing.eurekaclient2.web.service.TbCertkeyService;
import com.yunjing.eurekaclient2.web.service.TbEsealExpireService;
import com.yunjing.eurekaclient2.web.service.TbEsealService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
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

    private String USER_TYPE_OPERATOR= "OPERATOR";
    private String USER_TYPE_PAAS_CLIENT = "PAAS_CLIENT";

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
            return ResultInfo.ok().put("content", Base64.getUrlEncoder().encodeToString(content)).put("sealStatus", eseal.getStatus()).put("keyID", keyid);
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
    public ResultInfo updateEseal(@RequestParam("userId")String userId, @RequestParam("oldEsSN")int oldEsSN){
        TbEseal eseal = tbEsealService.updateEseal(oldEsSN);
        if(eseal == null){
            return ResultInfo.error("update eseal " + oldEsSN + "error");
        }
        return ResultInfo.ok().put("esSN", eseal.getId()).put("esId", eseal.getEsId());
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

}
