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
    public ResultInfo generateEseal(@RequestParam("creatorId")String creatorID,@RequestParam("creatorType")String creatorType, @RequestParam("type")int type,
                                    @RequestParam(value = "userId",defaultValue = "", required = false)String userID,
                                    @RequestParam("name")String name,@RequestParam("usage")String usage,@RequestParam("esId")String esId,
                                    @RequestParam(value = "pic",defaultValue = "", required = false)String pic,
                                    @RequestParam(value = "createPicType",defaultValue = "0", required = false)int createPicType,
                                    @RequestParam(value ="validEnd",defaultValue = "", required = false)String validEnd,@RequestParam("isScene")String isScene){

        TbEseal eseal =tbEsealService.generate(creatorID, creatorType, type, userID, name, usage, esId, pic, createPicType, validEnd, isScene);
        return ResultInfo.ok().put("esSN", eseal.getId()).put("esId", eseal.getEsId());

    }



}
