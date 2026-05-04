package com.vending.module.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vending.common.cache.RedisCacheUtil;
import com.vending.common.exception.BusinessException;
import com.vending.common.result.ResultCode;
import com.vending.common.util.JwtUtil;
import com.vending.module.user.dto.LoginRequest;
import com.vending.module.user.dto.RegisterRequest;
import com.vending.module.user.dto.UserVO;
import com.vending.module.user.entity.User;
import com.vending.module.user.mapper.UserMapper;
import com.vending.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisCacheUtil redisCacheUtil;

    @Override
    public void register(RegisterRequest request) {
        log.info("开始注册用户: username={}, phone={}", request.getUsername(), request.getPhone());
        
        if (this.lambdaQuery().eq(User::getUsername, request.getUsername()).exists()) {
            log.warn("用户名已存在: {}", request.getUsername());
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(0);
        user.setStatus(1);

        log.info("保存用户到数据库: {}", user);
        this.save(user);
        log.info("用户注册成功: userId={}", user.getUserId());
    }

    @Override
    public Map<String, Object> login(LoginRequest request) {
        User user = this.lambdaQuery()
                .eq(User::getUsername, request.getUsername())
                .one();

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_WRONG);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getUsername());

        // 保存 Refresh Token 到 Redis，过期时间和 Token 一致
        redisCacheUtil.set(RedisCacheUtil.KEY_REFRESH_TOKEN + user.getUserId(), refreshToken, 7, TimeUnit.DAYS);

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("userId", user.getUserId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        return data;
    }

    /**
     * 刷新 Token
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            Long userId = jwtUtil.getUserId(refreshToken);
            String tokenType = jwtUtil.getTokenType(refreshToken);

            // 验证 Token 类型
            if (!"refresh".equals(tokenType)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }

            // 验证 Token 是否在 Redis 中
            String storedToken = redisCacheUtil.get(RedisCacheUtil.KEY_REFRESH_TOKEN + userId, String.class);
            if (!refreshToken.equals(storedToken)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }

            User user = this.getById(userId);
            String newAccessToken = jwtUtil.generateAccessToken(userId, user.getUsername(), user.getRole());

            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", newAccessToken);
            return data;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    /**
     * 登出
     */
    public void logout(String accessToken, String refreshToken, Long userId) {
        // 将 Access Token 加入黑名单，过期时间和 Token 一致
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                long ttl = jwtUtil.getExpiration(accessToken);
                if (ttl > 0) {
                    redisCacheUtil.set(RedisCacheUtil.KEY_JWT_BLACKLIST + accessToken, "1", ttl, TimeUnit.MILLISECONDS);
                }
            } catch (Exception ignored) {}
        }

        // 删除 Refresh Token
        redisCacheUtil.delete(RedisCacheUtil.KEY_REFRESH_TOKEN + userId);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setRealName(user.getRealName());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public void updateUserInfo(Long userId, UserVO userVO) {
        User user = new User();
        user.setUserId(userId);
        user.setPhone(userVO.getPhone());
        user.setEmail(userVO.getEmail());
        user.setRealName(userVO.getRealName());
        user.setAvatar(userVO.getAvatar());
        this.updateById(user);
    }
}
