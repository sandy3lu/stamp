package com.yunjing.eurekaclient2.stream.listener;

import com.yunjing.eurekaclient2.stream.channel.DefaultProcess;
import com.yunjing.eurekaclient2.web.entity.DictConstant;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;

/**
 * @ClassName MyMessageListener
 * @Description 消息消费者
 * @Author scyking
 * @Date 2019/1/27 17:51
 * @Version 1.0
 */
@EnableBinding(DefaultProcess.class)
public class MyMessageListener {

    @StreamListener(DefaultProcess.INPUT)
    public void input(Message<DictConstant> message) {
        System.err.println("【*** 消息接收:MyMessageListener ***】" + message.getPayload());
    }
}
