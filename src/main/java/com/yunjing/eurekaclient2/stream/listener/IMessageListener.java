package com.yunjing.eurekaclient2.stream.listener;

import com.yunjing.eurekaclient2.web.entity.DictConstant;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;

/**
 * @ClassName IMessageListener
 * @Description 消息消费者
 * @Author scyking
 * @Date 2019/1/25 17:12
 * @Version 1.0
 */
@EnableBinding(Sink.class) // sink：通过指定消费信息的目标来标识消息的使用者约定
public class IMessageListener {

    @StreamListener(Sink.INPUT)
    public void input(Message<DictConstant> message) {
        System.err.println("【*** 消息接收:IMessageListener ***】" + message.getPayload());
    }
}
