package com.yunjing.eurekaclient2.web.vo;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

public class TbEsealVO {
    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String esID;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private int type;

    @Getter
    @Setter
    private String userID;

    @Getter
    @Setter
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Getter
    @Setter
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime validEnd;

    @Getter
    @Setter
    private int status;

}
