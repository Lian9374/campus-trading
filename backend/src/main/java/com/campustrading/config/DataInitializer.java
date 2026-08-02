package com.campustrading.config;

import com.campustrading.entity.Category;
import com.campustrading.entity.User;
import com.campustrading.repository.CategoryRepository;
import com.campustrading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 初始化默认数据（跨 MySQL/PostgreSQL，替代 data.sql）。
 * 1. 默认管理员账号（仅在不存在时创建）
 * 2. 默认商品分类（仅在分类表为空时创建）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        initAdmin();
        initCategories();
    }

    private void initAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            log.info("默认管理员账号已创建 — 用户名: admin, 密码: admin123");
        }
    }

    private void initCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }
        // id 由数据库自增，保证 MySQL/PostgreSQL 兼容
        Category[] categories = {
            cat("数码电子"),
            cat("书籍教材"),
            cat("生活用品"),
            cat("服饰鞋包"),
            cat("运动户外"),
            cat("其他")
        };
        categoryRepository.saveAll(List.of(categories));
        log.info("默认商品分类已创建 — {} 个", categories.length);
    }

    private Category cat(String name) {
        Category c = new Category();
        c.setName(name);
        return c;
    }
}
