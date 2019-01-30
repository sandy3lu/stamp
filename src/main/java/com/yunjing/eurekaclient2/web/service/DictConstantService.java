package com.yunjing.eurekaclient2.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunjing.eurekaclient2.web.entity.DictConstant;

/**
 * <p>
 * 通用字典表 服务类
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-23
 */
public interface DictConstantService extends IService<DictConstant> {

    /**
     * 分页查询
     *
     * @param page
     * @param value
     * @return
     */
    IPage<DictConstant> selectPageVO(Page page, String value);

    /**
     * 实体查询
     *
     * @param value
     * @return
     */
    DictConstant selectDictConstant(String value);

}
