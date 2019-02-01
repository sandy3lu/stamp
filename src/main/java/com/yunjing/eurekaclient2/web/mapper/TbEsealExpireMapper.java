package com.yunjing.eurekaclient2.web.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunjing.eurekaclient2.web.entity.TbEseal;
import com.yunjing.eurekaclient2.web.entity.TbEsealExpire;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * <p>
 * 过期和注销的印章数据表 Mapper 接口
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-30
 */
public interface TbEsealExpireMapper extends BaseMapper<TbEsealExpire> {
    IPage<TbEsealExpire> selectPageVO(Page<TbEseal> pageInfo, @Param("type")Integer type, @Param("status")Integer status,
                                @Param("esId")String esId, @Param("name")String name, @Param("userId")String userId);

}
