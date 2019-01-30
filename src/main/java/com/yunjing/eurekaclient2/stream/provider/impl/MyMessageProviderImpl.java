package com.yunjing.eurekaclient2.stream.provider.impl;

import com.yunjing.eurekaclient2.stream.channel.DefaultProcess;
import com.yunjing.eurekaclient2.stream.provider.MyMessageProvider;
import com.yunjing.eurekaclient2.web.entity.DictConstant;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import javax.annotation.Resource;

/**
 * @ClassName MyMessageProviderImpl
 * @Description 自定义
 * @Author scyking
 * @Date 2019/1/27 17:47
 * @Version 1.0
 */
@EnableBinding(DefaultProcess.class) // 使用自定义通道
public class MyMessageProviderImpl implements MyMessageProvider {

    @Resource
    private MessageChannel output;

    @Override
    public void send(DictConstant dictConstant) {
        this.output.send(MessageBuilder.withPayload(dictConstant).build());
    }
}
