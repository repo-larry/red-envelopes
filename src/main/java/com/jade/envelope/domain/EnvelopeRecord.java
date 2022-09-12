package com.jade.envelope.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class EnvelopeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer account;

    private String nickName;

    private Integer uid;

    private Long envelopeId;

    private LocalDateTime createTime;
}
