package com.vending.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.vending.module.user.dto.LoginRequest;
import com.vending.module.user.dto.RegisterRequest;
import com.vending.module.user.dto.UserVO;
import com.vending.module.user.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {
    void register(RegisterRequest request);
    Map<String, Object> login(LoginRequest request);
    UserVO getUserInfo(Long userId);
    void updateUserInfo(Long userId, UserVO userVO);
}
