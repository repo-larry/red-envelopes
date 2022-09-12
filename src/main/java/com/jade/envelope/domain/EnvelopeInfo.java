package com.jade.envelope.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName(value = "envelope_info", autoResultMap = true, schema = "public")
public class EnvelopeInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    // timestamp+5位随机数
    private Long envelopeId;

    private Integer account;

    private Integer number;

    private Integer remainingAmount;

    private Integer remainingNumber;

    private Integer uid;

    private LocalDateTime createTime;

    private Long keepTime;

    private LocalDateTime updateTime;

    private Integer status;
}
