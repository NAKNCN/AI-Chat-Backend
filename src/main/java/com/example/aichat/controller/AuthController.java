package com.example.aichat.controller;

import com.example.aichat.mapper.UserMapper;
import com.example.aichat.pojo.LoginRequest;
import com.example.aichat.pojo.RegisterRequest;
import com.example.aichat.pojo.Result;
import com.example.aichat.pojo.User;
import com.example.aichat.utils.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthController(UserMapper userMapper,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/auth/register")
    public Result register(@RequestBody RegisterRequest request) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", request.getUsername());
        if (userMapper.selectOne(wrapper) != null) {
            return Result.fail(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.insert(user);
        return Result.success("注册成功");
    }

    @PostMapping("/auth/login")
    public Result login(@RequestBody LoginRequest request) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", request.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return Result.fail(401, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.fail(401, "用户名或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        return Result.success(token);
    }
}