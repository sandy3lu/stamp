package com.yunjing.eurekaclient2.feign.remote;

import com.yunjing.eurekaclient2.common.base.ResultInfo;
import com.yunjing.eurekaclient2.feign.hystrix.SignServiceRemoteHystrix;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "pms-signer", fallback = SignServiceRemoteHystrix.class)
public interface SignServiceRemote {

    @GetMapping("/v1.0/verification")
    ResultInfo verify(@RequestParam("content") String content, @RequestParam("signature") String signature, @RequestParam("algorithmID") String algorithmID, @RequestParam("publicKey") String publicKey) ;

    @PostMapping("/v1.0/signaturewithkeys")
    ResultInfo signWithKey(@RequestParam("content") String content,@RequestParam("algorithmID") String algorithmID,@RequestParam("privateKey") String privateKey,@RequestParam("publicKey") String publicKey);

    @PostMapping("/v1.0/signaturewithid")
    ResultInfo sign(@RequestParam("userId")String userID, @RequestParam("content") String content, @RequestParam("keyID") int keyID);

}
