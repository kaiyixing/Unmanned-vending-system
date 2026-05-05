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
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate redisTemplate;

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

        // 使用 jti 作为 Refresh Token 的 key 后缀，支持多设备登录
        String refreshTokenId = jwtUtil.getTokenId(refreshToken);
        redisCacheUtil.set(RedisCacheUtil.KEY_REFRESH_TOKEN + user.getUserId() + ":" + refreshTokenId, 
                          refreshToken, 7, TimeUnit.DAYS);

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("userId", user.getUserId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        return data;
    }

    /**
     * 刷新 Token（实现 Token 旋转，刷新时同时返回新的 Refresh Token）
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        try {
            Long userId = jwtUtil.getUserId(refreshToken);
            String tokenType = jwtUtil.getTokenType(refreshToken);
            String refreshTokenId = jwtUtil.getTokenId(refreshToken);

            // 验证 Token 类型
            if (!"refresh".equals(tokenType)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }

            // 验证 Token 是否在 Redis 中（使用 userId + jti）
            String refreshTokenKey = RedisCacheUtil.KEY_REFRESH_TOKEN + userId + ":" + refreshTokenId;
            String storedToken = redisCacheUtil.get(refreshTokenKey, String.class);
            if (!refreshToken.equals(storedToken)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }

            // 删除旧的 Refresh Token（实现一次性使用）
            redisCacheUtil.delete(refreshTokenKey);

            // 生成新的 Token
            User user = this.getById(userId);
            String newAccessToken = jwtUtil.generateAccessToken(userId, user.getUsername(), user.getRole());
            String newRefreshToken = jwtUtil.generateRefreshToken(userId, user.getUsername());
            String newRefreshTokenId = jwtUtil.getTokenId(newRefreshToken);

            // 保存新的 Refresh Token
            redisCacheUtil.set(RedisCacheUtil.KEY_REFRESH_TOKEN + userId + ":" + newRefreshTokenId, 
                              newRefreshToken, 7, TimeUnit.DAYS);

            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", newAccessToken);
            data.put("refreshToken", newRefreshToken);
            return data;
        } catch (Exception e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    /**
     * 登出
     */
    public void logout(String accessToken, String refreshToken, Long userId) {
        // 将 Access Token 加入黑名单，使用 jti 作为 key
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                String accessTokenId = jwtUtil.getTokenId(accessToken);
                long ttl = jwtUtil.getExpiration(accessToken);
                if (ttl > 0) {
                    redisCacheUtil.set(RedisCacheUtil.KEY_JWT_BLACKLIST + accessTokenId, "1", ttl, TimeUnit.MILLISECONDS);
                }
            } catch (Exception ignored) {}
        }

        // 删除 Refresh Token（使用 userId + jti）
        if (refreshToken != null && !refreshToken.isEmpty()) {
            try {
                String refreshTokenId = jwtUtil.getTokenId(refreshToken);
                redisCacheUtil.delete(RedisCacheUtil.KEY_REFRESH_TOKEN + userId + ":" + refreshTokenId);
            } catch (Exception ignored) {}
        }
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
