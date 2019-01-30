package com.yunjing.eurekaclient2.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunjing.eurekaclient2.web.entity.DictConstant;
import com.yunjing.eurekaclient2.web.mapper.DictConstantMapper;
import com.yunjing.eurekaclient2.web.service.DictConstantService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通用字典表 服务实现类
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-23
 */
@Service
public class DictConstantServiceImpl extends ServiceImpl<DictConstantMapper, DictConstant> implements DictConstantService {

    @Override
    public IPage<DictConstant> selectPageVO(Page page, String value) {
        return this.baseMapper.selectPageVO(page, value);
    }

    @Override
    public DictConstant selectDictConstant(String value) {
        QueryWrapper<DictConstant> wrapper = new QueryWrapper<>();
        wrapper.eq("value", value);// 注意，column 参数对应的是。
        return this.getOne(wrapper);
    }


}
