package com.vending.module.user.dto;

import lombok.Data;

@Data
public class UserVO {
    private Long userId;
    private String username;
    private String phone;
    private String email;
    private String realName;
    private String avatar;
    private Integer role;
}
