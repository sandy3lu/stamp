package com.yunjing.eurekaclient2.common.picGenerate;

public class OperationException extends RuntimeException {
    public OperationException(String code, String msg){
        super("error code :" + code + "  " + msg);
    }
}
