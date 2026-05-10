package com.sports.config;

import com.sports.entity.ExerciseType;
import com.sports.entity.User;
import com.sports.repository.ExerciseTypeRepository;
import com.sports.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器 - 初始化运动类型和测试账号
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private ExerciseTypeRepository exerciseTypeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        initExerciseTypes();
        initTestUsers();
    }
    
    private void initExerciseTypes() {
        if (exerciseTypeRepository.count() > 0) {
            logger.info("运动类型数据已存在，跳过初始化");
            return;
        }
        
        logger.info("开始初始化运动类型数据...");
        
        exerciseTypeRepository.save(createType("跑步", "🏃", 10, "有氧运动，改善心肺功能", "#FF6B6B"));
        exerciseTypeRepository.save(createType("骑行", "🚴", 8, "低冲击有氧运动", "#4ECDC4"));
        exerciseTypeRepository.save(createType("游泳", "🏊", 12, "全身运动，锻炼核心肌群", "#45B7D1"));
        exerciseTypeRepository.save(createType("瑜伽", "🧘", 4, "提升柔韧性和平衡性", "#96CEB4"));
        exerciseTypeRepository.save(createType("健身", "💪", 7, "力量训练，增强肌肉", "#FFEAA7"));
        exerciseTypeRepository.save(createType("篮球", "🏀", 9, "团队运动，全身锻炼", "#DDA0DD"));
        exerciseTypeRepository.save(createType("足球", "⚽", 11, "高强度有氧运动", "#98D8C8"));
        exerciseTypeRepository.save(createType("羽毛球", "🏸", 8, "速度与反应训练", "#F7DC6F"));
        exerciseTypeRepository.save(createType("网球", "🎾", 9, "全身协调运动", "#BB8FCE"));
        exerciseTypeRepository.save(createType("徒步", "🥾", 5, "户外有氧，亲近自然", "#82E0AA"));
        exerciseTypeRepository.save(createType("跳绳", "⏱️", 13, "高效燃脂运动", "#F1948A"));
        exerciseTypeRepository.save(createType("舞蹈", "💃", 6, "有氧与艺术结合", "#D7BDE2"));
        
        logger.info("运动类型数据初始化完成，共{}条", exerciseTypeRepository.count());
    }
    
    private void initTestUsers() {
        // 创建管理员账号
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setEmail("admin@sports.com");
            admin.setNickname("管理员");
            userRepository.save(admin);
            logger.info("创建测试账号: admin / 123456");
        }
        
        // 创建普通用户账号
        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setEmail("user@sports.com");
            user.setNickname("测试用户");
            userRepository.save(user);
            logger.info("创建测试账号: user / 123456");
        }
    }
    
    private ExerciseType createType(String name, String icon, int calories, String desc, String color) {
        ExerciseType type = new ExerciseType();
        type.setName(name);
        type.setIcon(icon);
        type.setCaloriesPerMinute(calories);
        type.setDescription(desc);
        type.setColor(color);
        return type;
    }
}
