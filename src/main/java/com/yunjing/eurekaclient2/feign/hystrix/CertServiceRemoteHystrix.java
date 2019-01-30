package com.yunjing.eurekaclient2.feign.hystrix;

import com.yunjing.eurekaclient2.common.base.CertInfo;
import com.yunjing.eurekaclient2.feign.remote.CertServiceRemote;
import org.springframework.stereotype.Component;

/**
 * @ClassName Client1RemoteHystrix
 * @Description 回调类
 * @Author scyking
 * @Date 2019/1/21 16:23
 * @Version 1.0
 */
@Component
public class CertServiceRemoteHystrix implements CertServiceRemote {


    /**
     * 请求形式（地址、参数、返回值类型）需与接口提供方保持一致，具有映射关系
     *
     * @param certInfo
     * @param userID
     * @return
     */
    @Override
    public String apply(CertInfo certInfo, String userID) {
        return "CertServiceRemote, this message send failed !";
    }

    @Override
    public String validate(String certNo, String userID) {
        return "CertServiceRemote, this message send failed !";
    }
}
