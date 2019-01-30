package com.yunjing.eurekaclient2.web.controller;


import com.yunjing.eurekaclient2.common.base.ResultInfo;
import com.yunjing.eurekaclient2.web.entity.TbEseal;
import com.yunjing.eurekaclient2.web.service.TbEsealService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/eseal")
    @ApiOperation("产生印章")
    public ResultInfo generateEseal(@RequestParam("creatorId")String creatorID,@RequestParam("userType")String userType, @RequestParam("type")int type,
                                    @RequestParam(value = "userId")String userID,
                                    @RequestParam("name")String name,@RequestParam("usage")String usage,@RequestParam("esId")String esId,
                                    @RequestParam(value = "pic",defaultValue = "", required = false)String pic,
                                    @RequestParam(value = "createPic",defaultValue = "false", required = false)String createPic,
                                    @RequestParam("validEnd")String validEnd,@RequestParam("isScene")String isScene){

        TbEseal eseal =tbEsealService.generate(creatorID, userType, type, userID, name, usage, esId, pic, createPic, validEnd, isScene);
        return ResultInfo.ok().put("esSN", eseal.getId()).put("esId", eseal.getEsId());

    }



}
