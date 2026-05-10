package com.sports.service;

import com.sports.dto.LoginRequest;
import com.sports.dto.LoginResponse;
import com.sports.dto.RegisterRequest;
import com.sports.entity.User;
import com.sports.exception.BusinessException;
import com.sports.repository.UserRepository;
import com.sports.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Transactional
    public User register(RegisterRequest request) {
        logger.info("用户注册请求: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        
        User savedUser = userRepository.save(user);
        logger.info("用户注册成功: {}", savedUser.getUsername());
        
        return savedUser;
    }
    
    public LoginResponse login(LoginRequest request) {
        logger.info("用户登录请求: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        String token = tokenProvider.generateToken(user.getUsername());
        logger.info("用户登录成功: {}", user.getUsername());
        
        return new LoginResponse(
                token,
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getId()
        );
    }
}
