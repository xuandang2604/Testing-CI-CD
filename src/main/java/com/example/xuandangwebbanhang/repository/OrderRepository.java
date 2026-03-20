package com.example.xuandangwebbanhang.repository;

import com.example.xuandangwebbanhang.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}