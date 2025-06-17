package com.example.springsocial.controller;

import com.example.springsocial.domain.ApiResult;
import com.example.springsocial.model.AuthProvider;
import com.example.springsocial.model.User;
import com.example.springsocial.payload.LoginRequest;
import com.example.springsocial.payload.SignUpRequest;
import com.example.springsocial.repository.UserRepository;
import com.example.springsocial.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    @PostMapping("/login")
    public ApiResult<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        ApiResult<String> apiResult = ApiResult.newInstance();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.createToken(authentication);
        return apiResult.succeed().data(token);
    }

    @PostMapping("/signup")
    public ApiResult<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        ApiResult<Boolean> apiResult = ApiResult.newInstance();

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return apiResult.failed().message("Email address already in use.");
        }

        // Creating user's account
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(signUpRequest.getPassword());
        user.setProvider(AuthProvider.local);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/user/me")
                .buildAndExpand(result.getId()).toUri();
        return apiResult.succeed();
    }


    public static void main(String[] args) {

        // 创建 Spring Security 的密码编码器
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        String rawPassword = "123456";  // 要加密的原始密码

        // 加密密码
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 输出结果
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后: " + encodedPassword);
        System.out.println("长度: " + encodedPassword.length() + " 字符");

        // 验证密码
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("验证结果: " + (matches ? "成功" : "失败"));
    }
}
