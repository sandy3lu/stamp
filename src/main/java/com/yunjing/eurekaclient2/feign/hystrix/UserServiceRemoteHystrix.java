package com.yunjing.eurekaclient2.feign.hystrix;

import com.yunjing.eurekaclient2.feign.remote.UserServiceRemote;
import org.springframework.stereotype.Component;

@Component
public class UserServiceRemoteHystrix implements UserServiceRemote {
    /**
     * 请求形式（地址、参数、返回值类型）需与接口提供方保持一致，具有映射关系
     *
     * @param userID
     * @return
     */
    @Override
    public String getUserInfo(String userID) {
        return "UserServiceRemote, this message send failed !";
    }

    @Override
    public String getEnterpriseInfo(String userID) {
        return "UserServiceRemote, this message send failed !";
    }
}
