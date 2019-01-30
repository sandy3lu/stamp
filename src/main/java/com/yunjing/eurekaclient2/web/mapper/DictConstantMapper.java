package com.yunjing.eurekaclient2.web.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunjing.eurekaclient2.web.entity.DictConstant;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 通用字典表 Mapper 接口
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-23
 */
public interface DictConstantMapper extends BaseMapper<DictConstant> {

    /**
     * 分页查询
     *
     * @param page  必须放在第一个参数
     * @param value
     * @return
     */
    IPage<DictConstant> selectPageVO(Page page, @Param("value") String value);

}
