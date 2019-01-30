package com.yunjing.eurekaclient2.stream.provider.impl;

import com.yunjing.eurekaclient2.stream.provider.IMessageProvider;
import com.yunjing.eurekaclient2.web.entity.DictConstant;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import javax.annotation.Resource;

/**
 * @ClassName IMessageProviderImpl
 * @Description 发送消息测试
 * @Author scyking
 * @Date 2019/1/25 16:46
 * @Version 1.0
 */
@EnableBinding(Source.class) // source：与sink相反，用于标识消息生产者的约定，可以理解为是一个消息的发送管道的定义。
public class IMessageProviderImpl implements IMessageProvider {

    /**
     * 消息的发送管道
     */
    @Resource
    private MessageChannel output;

    @Override
    public void send(DictConstant dictConstant) {
        // 创建并发送消息
        this.output.send(MessageBuilder.withPayload(dictConstant).build());
    }
}
