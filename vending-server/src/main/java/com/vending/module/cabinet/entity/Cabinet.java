package com.vending.module.cabinet.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cabinet")
public class Cabinet {
    @TableId(type = IdType.AUTO)
    private Long cabinetId;
    private String cabinetCode;
    private String name;
    private String city;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer capacity;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
