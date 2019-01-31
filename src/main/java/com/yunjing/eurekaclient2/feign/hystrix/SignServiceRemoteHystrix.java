package com.yunjing.eurekaclient2.feign.hystrix;

import com.yunjing.eurekaclient2.common.base.ResultInfo;
import com.yunjing.eurekaclient2.feign.remote.SignServiceRemote;
import org.springframework.stereotype.Component;

@Component
public class SignServiceRemoteHystrix implements SignServiceRemote {
    @Override
    public ResultInfo verify(String content, String signature, String algorithmID, String publicKey) {
        return ResultInfo.error("UserServiceRemote, this message send failed !");
    }

    @Override
    public ResultInfo signWithKey(String content, String algorithmID, String privateKey, String publicKey) {
        return ResultInfo.error("UserServiceRemote, this message send failed !");
    }

    @Override
    public ResultInfo sign(String userID, String content, int keyID) {
        return ResultInfo.error("UserServiceRemote, this message send failed !");
    }
}
