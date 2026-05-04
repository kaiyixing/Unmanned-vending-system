package com.vending.module.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vending.common.result.Result;
import com.vending.module.user.dto.LoginRequest;
import com.vending.module.user.dto.RegisterRequest;
import com.vending.module.user.dto.UserVO;
import com.vending.module.user.entity.User;
import com.vending.module.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> data = userService.login(request);
        // 兼容旧接口，同时返回 token 和 accessToken
        if (data.containsKey("accessToken") && !data.containsKey("token")) {
            data.put("token", data.get("accessToken"));
        }
        return Result.success(data);
    }

    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        Map<String, Object> data = userService.refreshToken(refreshToken);
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestAttribute("userId") Long userId,
                               @RequestBody(required = false) Map<String, String> body,
                               HttpServletRequest request) {
        String accessToken = null;
        String refreshToken = null;

        // 从 Authorization Header 中获取 Access Token
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            accessToken = header.substring(7);
        }

        // 从请求体获取 Refresh Token
        if (body != null) {
            refreshToken = body.get("refreshToken");
        }

        userService.logout(accessToken, refreshToken, userId);
        return Result.success();
    }

    @GetMapping("/info")
    public Result<UserVO> getUserInfo(@RequestAttribute("userId") Long userId) {
        UserVO user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    @PutMapping("/info")
    public Result<Void> updateUserInfo(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UserVO userVO) {
        userService.updateUserInfo(userId, userVO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<User>> list() {
        List<User> users = userService.list();
        return Result.success(users);
    }
}
