package com.jade.envelope.domain;

import lombok.Data;

@Data
public class EnvelopeSaveDTO {

    private Integer uid;
    private Integer number;
    private Integer account;
    private long keepTime;

}
