package com.yunjing.eurekaclient2.stream.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

// 输入输出最好不要定义在同一个接口中，此处为方便测试，未分别定义
public interface DefaultProcess {

    String OUTPUT = "my_output";
    String INPUT = "my_input";

    @Input(DefaultProcess.INPUT)
    SubscribableChannel input();

    @Output(DefaultProcess.OUTPUT)
    MessageChannel output();
}
