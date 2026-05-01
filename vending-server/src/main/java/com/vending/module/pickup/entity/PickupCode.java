package com.vending.module.pickup.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("pickup_code")
public class PickupCode {
    @TableId(type = IdType.AUTO)
    private Long codeId;
    private Long orderId;
    private String orderNo;
    private String codeValue;
    private String qrCodeUrl;
    private Integer status;
    private LocalDateTime expireTime;
    private LocalDateTime useTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
