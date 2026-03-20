package com.example.xuandangwebbanhang.config;

import com.example.xuandangwebbanhang.model.Role;
import com.example.xuandangwebbanhang.repository.CategoryRepository;
import com.example.xuandangwebbanhang.repository.IRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(CategoryRepository categoryRepository,
                                      IRoleRepository roleRepository) {
        return args -> {
            // Kiểm tra theo TÊN, KHÔNG set ID cứng – tránh lỗi ObjectOptimisticLockingFailureException
            if (roleRepository.findByName("ADMIN").isEmpty()) {
                Role admin = new Role();
                admin.setName("ADMIN");
                admin.setDescription("Quản trị viên – toàn quyền");
                roleRepository.save(admin);
            }
            if (roleRepository.findByName("MANAGER").isEmpty()) {
                Role manager = new Role();
                manager.setName("MANAGER");
                manager.setDescription("Quản lý – chỉ quản lý sản phẩm & danh mục");
                roleRepository.save(manager);
            }
            if (roleRepository.findByName("USER").isEmpty()) {
                Role user = new Role();
                user.setName("USER");
                user.setDescription("Người dùng thông thường");
                roleRepository.save(user);
            }

            // Giữ nguyên DB do người dùng nhập danh mục; không seed mẫu để tránh set cứng 3 cấp
            if (categoryRepository.count() > 0) {
                return;
            }
            // Không tạo dữ liệu mẫu. Người dùng tự nhập danh mục (các cấp và icon) trong UI.
        };
    }
}
