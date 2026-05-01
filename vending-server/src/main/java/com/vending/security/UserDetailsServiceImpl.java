package com.vending.security;

import com.vending.module.user.entity.User;
import com.vending.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.lambdaQuery()
                .eq(User::getUsername, username)
                .one();

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        String roleName = getRoleName(user.getRole());
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + roleName)
        );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1,
                true, true, true,
                authorities
        );
    }

    private String getRoleName(Integer role) {
        return switch (role) {
            case 2 -> "SUPER_ADMIN";
            case 1 -> "ADMIN";
            default -> "USER";
        };
    }
}
