package com.yunjing.eurekaclient2.web.service;

import com.yunjing.eurekaclient2.web.entity.TbEseal;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 正在使用的印章数据表 服务类
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-30
 */
public interface TbEsealService extends IService<TbEseal> {

    int TYPE_LEGAL_NAME = 1;
    int TYPE_FINANCE = 2;
    int TYPE_ENVOICE = 3;
    int TYPE_CONTRACT = 4;
    int TYPE_PERSONAL =5;

    TbEseal generate(String creatorID, String userType, int type, String userID, String name, String usage, String esId, String pic, String createPic, String validEnd, String isScene);
}
