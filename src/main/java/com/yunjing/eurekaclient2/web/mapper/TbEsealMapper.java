package com.yunjing.eurekaclient2.web.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunjing.eurekaclient2.web.entity.TbEseal;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * <p>
 * 正在使用的印章数据表 Mapper 接口
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-30
 */
public interface TbEsealMapper extends BaseMapper<TbEseal> {

    IPage<TbEseal> selectPageVO(Page<TbEseal> pageInfo, @Param("type")Integer type, @Param("status")Integer status,
                                @Param("esId")String esId, @Param("name")String name, @Param("userId")String userId,
                                @Param("ref") LocalDateTime ref);
}
