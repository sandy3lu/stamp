package com.yunjing.eurekaclient2.common.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CertInfo {

    String name;
    String id;
    String phoneNumber;
    String email;
    String image;
    int type;
}
