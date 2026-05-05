package com.vending.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vending.common.cache.RedisCacheUtil;
import com.vending.common.result.Result;
import com.vending.common.result.ResultCode;
import com.vending.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisCacheUtil redisCacheUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                // 先解析 token 获取 jti，再检查黑名单
                String tokenId = jwtUtil.getTokenId(token);
                
                // 检查是否在黑名单
                if (redisCacheUtil.exists(RedisCacheUtil.KEY_JWT_BLACKLIST + tokenId)) {
                    sendError(response, ResultCode.TOKEN_INVALID);
                    return;
                }

                // 验证 token 类型必须是 access
                String tokenType = jwtUtil.getTokenType(token);
                if (!"access".equals(tokenType)) {
                    sendError(response, ResultCode.TOKEN_INVALID);
                    return;
                }

                Long userId = jwtUtil.getUserId(token);
                String username = jwtUtil.parseToken(token).getSubject();
                Integer role = jwtUtil.getRole(token);

                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + getRoleName(role))
                );

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                request.setAttribute("userId", userId);
                request.setAttribute("role", role);
            } catch (Exception e) {
                // Token 无效，不设置认证信息，由 Spring Security 处理未授权
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        Result<Void> result = Result.fail(resultCode);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private String getRoleName(Integer role) {
        return switch (role) {
            case 2 -> "SUPER_ADMIN";
            case 1 -> "ADMIN";
            default -> "USER";
        };
    }
}
