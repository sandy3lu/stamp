package com.yunjing.eurekaclient2.web.service;

import com.yunjing.eurekaclient2.web.entity.TbEseal;
import com.yunjing.eurekaclient2.web.entity.TbEsealExpire;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 过期和注销的印章数据表 服务类
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-30
 */
public interface TbEsealExpireService extends IService<TbEsealExpire> {
    TbEsealExpire get(int esSN);

    boolean insert(TbEseal eseal);
}
