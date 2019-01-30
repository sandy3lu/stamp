package com.yunjing.eurekaclient2.feign.remote;

import com.yunjing.eurekaclient2.common.base.CertInfo;
import com.yunjing.eurekaclient2.feign.hystrix.CertServiceRemoteHystrix;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 服务远程调用
 *
 * <p>
 *
 * @FeignClient name值为注册中心注册服务名，fallback为请求失败回调类
 * </p>
 */
@FeignClient(name = "pms-cert", fallback = CertServiceRemoteHystrix.class)
public interface CertServiceRemote {

    /**
     * 请求形式（地址、参数、返回值类型）需与接口提供方保持一致，具有映射关系
     *
     * @return
     */
    @PostMapping ("/v1.0/cert/apply")
    String apply(@RequestParam("certInfo") final CertInfo certInfo,@RequestParam("userId") final String userID);

    @PostMapping ("/v1.0/cert/validate")
    String validate(@RequestParam("certNo") final String certNo,@RequestParam("userId") final String userID);
}
