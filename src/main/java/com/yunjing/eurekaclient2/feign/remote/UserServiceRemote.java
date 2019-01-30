package com.yunjing.eurekaclient2.feign.remote;


import com.yunjing.eurekaclient2.feign.hystrix.UserServiceRemoteHystrix;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "pms-user", fallback = UserServiceRemoteHystrix.class)
public interface UserServiceRemote {

    /**
     * 请求形式（地址、参数、返回值类型）需与接口提供方保持一致，具有映射关系
     *
     * @return
     */
    @GetMapping("/v1.0/users/{userId}/paas-client")
    String getUserInfo(@PathVariable("userId") final String userID);

    @GetMapping("/v1.0/enterprise-identities/{userId}")
    String getEnterpriseInfo(@PathVariable("userId") final String userID);

    @GetMapping("/v1.0/enterprise-identitie")
    String getSelfInfo();
}
