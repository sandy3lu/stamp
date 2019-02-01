package com.yunjing.eurekaclient2.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunjing.eurekaclient2.web.entity.TbEseal;
import com.yunjing.eurekaclient2.web.entity.TbEsealExpire;
import com.yunjing.eurekaclient2.web.mapper.TbEsealExpireMapper;
import com.yunjing.eurekaclient2.web.service.TbEsealExpireService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunjing.eurekaclient2.web.service.TbEsealService;
import com.yunjing.eurekaclient2.web.vo.StatisticsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

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

    protected Logger logger = LoggerFactory.getLogger(getClass());

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
            TbEsealExpire tbEsealExpire = TbEsealExpire.getInstance(eseal);
            return this.save(tbEsealExpire);
        }
        return true;
    }

    @Override
    public StatisticsVO getStatics(String userType, String userID) {

        {
            StatisticsVO s = new StatisticsVO();
            int normal=0;
            int expire=0;
            int frozen = 0;
            int revoked = 0;
            int needRenew = 0;

            List<TbEsealExpire> list = null;
            switch (userType){
                case TbEsealService.USER_TYPE_OPERATOR:
                    // get all
                    list = this.list();
                    break;
                default:
                    // get userid's stamp
                    QueryWrapper<TbEsealExpire> wrapper = new QueryWrapper<>();
                    wrapper.eq("user_id",userID);
                    list = this.list(wrapper);
            }

            if((list == null) ||(list.size()<1)){
                return s;
            }
            Iterator<TbEsealExpire> it = list.iterator();
            while(it.hasNext()){
                TbEsealExpire tbEseal = it.next();
                int status = tbEseal.getStatus();
                switch (status){
                    case TbEsealService.STATUS_NORMAL:
                        logger.info(tbEseal.getId().toString() + " status is wrong in Expire table" );
                        break;
                    case TbEsealService.STATUS_EXPIRE:
                        expire++;
                        break;
                    case TbEsealService.STATUS_FROZEN:
                        frozen++;
                        break;
                    case TbEsealService.STATUS_REVOKED:
                        revoked++;
                        break;
                        default:
                            logger.info(tbEseal.getId().toString() + " status is wrong in Expire table" );
                }
            }

            s.setExpire(expire);
            s.setUsing(normal + needRenew);
            s.setFrozen(frozen);
            s.setNeedRenew(needRenew);
            s.setRevoked(revoked);
            return s;
        }
    }

    @Override
    public IPage<TbEsealExpire> selectPageVO(Page<TbEseal> pageInfo, Integer type, Integer status, String esId, String name, String userId) {
        return this.baseMapper.selectPageVO(pageInfo,  type,  status,  esId,  name,  userId);
    }
}
