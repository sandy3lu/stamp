package com.yunjing.eurekaclient2.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yunjing.eurekaclient2.web.entity.TbEseal;
import com.yunjing.eurekaclient2.web.entity.TbEsealExpire;
import com.yunjing.eurekaclient2.web.mapper.TbEsealExpireMapper;
import com.yunjing.eurekaclient2.web.service.TbEsealExpireService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 过期和注销的印章数据表 服务实现类
 * </p>
 *
 * @author scyking-auto
 * @since 2019-01-30
 */
@Service
public class TbEsealExpireServiceImpl extends ServiceImpl<TbEsealExpireMapper, TbEsealExpire> implements TbEsealExpireService {

    @Override
    public TbEsealExpire get(int esSN) {
        QueryWrapper<TbEsealExpire> wrapper = new QueryWrapper<>();
        wrapper.eq("id",esSN);
        TbEsealExpire key = this.getOne(wrapper);

        return key;
    }

    @Override
    public boolean insert(TbEseal eseal) {
        // check if already saved
        QueryWrapper<TbEsealExpire> wrapper = new QueryWrapper<>();
        wrapper.eq("id",eseal.getId());
        TbEsealExpire key = this.getOne(wrapper);
        if(key==null){
            // start insert
            TbEsealExpire tbEsealExpire = new TbEsealExpire();
            tbEsealExpire.setId(eseal.getId());
            tbEsealExpire.setEsId(eseal.getEsId());
            tbEsealExpire.setCreatorId(eseal.getCreatorId());
            tbEsealExpire.setUserId(eseal.getUserId());
            tbEsealExpire.setName(eseal.getName());
            tbEsealExpire.setUsage(eseal.getUsage());
            tbEsealExpire.setType(eseal.getType());
            tbEsealExpire.setStatus(eseal.getStatus());
            tbEsealExpire.setComment(eseal.getComment());
            tbEsealExpire.setCreateTime(eseal.getCreateTime());
            tbEsealExpire.setValidEnd(eseal.getValidEnd());
            tbEsealExpire.setContent(eseal.getContent());
            tbEsealExpire.setCertKeyList(eseal.getCertKeyList());
            return this.save(tbEsealExpire);
        }
        return true;
    }
}
