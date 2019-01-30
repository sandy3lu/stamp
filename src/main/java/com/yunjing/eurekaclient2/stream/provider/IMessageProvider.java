package com.yunjing.eurekaclient2.stream.provider;

import com.yunjing.eurekaclient2.web.entity.DictConstant;

/**
 * 消息发送接口
 */
public interface IMessageProvider {

    /**
     * 实现消息的发送，本次发送的消息是一个对象（自动变为json）
     *
     * @param dictConstant
     */
    void send(DictConstant dictConstant);
}
