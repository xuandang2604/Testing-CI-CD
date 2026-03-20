package com.example.xuandangwebbanhang.repository;

import com.example.xuandangwebbanhang.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}