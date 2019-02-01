package com.yunjing.eurekaclient2.web.vo;

import lombok.Getter;
import lombok.Setter;

public class StatisticsVO {

    @Getter
    @Setter
    private int using;

    @Getter
    @Setter
    private int frozen;


    @Getter
    @Setter
    private int revoked;


    @Getter
    @Setter
    private int needRenew;

    @Getter
    @Setter
    private int expire;


    public void add( StatisticsVO another){
        using = using + another.getUsing();
        frozen = frozen + another.getFrozen();
        revoked = revoked + another.getRevoked();
        needRenew = needRenew + another.getNeedRenew();
        expire = expire + another.getExpire();
    }
}
