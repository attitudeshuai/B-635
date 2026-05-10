package com.sports.service;

import com.sports.dto.PasswordChangeRequest;
import com.sports.dto.UserUpdateRequest;
import com.sports.entity.User;
import com.sports.exception.BusinessException;
import com.sports.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务
 */
@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
    
    @Transactional
    public User updateUser(Long userId, UserUpdateRequest request) {
        logger.info("更新用户信息: userId={}", userId);
        
        User user = getUserById(userId);
        
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            // 检查邮箱是否被其他用户使用
            userRepository.findByEmail(request.getEmail())
                    .filter(u -> !u.getId().equals(userId))
                    .ifPresent(u -> {
                        throw new BusinessException("邮箱已被其他用户使用");
                    });
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        
        return userRepository.save(user);
    }
    
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        logger.info("修改密码: userId={}", userId);
        
        User user = getUserById(userId);
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        logger.info("密码修改成功: userId={}", userId);
    }
}
