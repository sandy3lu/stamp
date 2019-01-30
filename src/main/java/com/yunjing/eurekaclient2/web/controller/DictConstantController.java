package com.yunjing.eurekaclient2.web.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunjing.eurekaclient2.web.entity.DictConstant;
import com.yunjing.eurekaclient2.web.service.DictConstantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 通用字典表 前端控制器
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-23
 */
@RestController
@RequestMapping("/web/dict-constant")
public class DictConstantController {

    @Autowired
    DictConstantService dictConstantService;

    /**
     * 模拟分页
     *
     * @return
     */
    @GetMapping("/list")
    public Object list() {
        int page = 1;
        int size = 10;
        Page<DictConstant> pageInfo = new Page<>(1, 10);
        IPage<DictConstant> list = dictConstantService.selectPageVO(pageInfo, "证书");
        return list;
    }
}
