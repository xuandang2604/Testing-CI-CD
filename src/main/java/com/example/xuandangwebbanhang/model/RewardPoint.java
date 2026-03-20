package com.example.xuandangwebbanhang.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "reward_points")
public class RewardPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên đăng nhập – định danh chính để tích/đổi điểm */
    @Column
    private String username;

    /** Số điện thoại (backup, từ form đặt hàng) */
    @Column(nullable = false)
    private String userPhone;

    /** Số điểm (âm = trừ điểm khi đổi) */
    @Column(nullable = false)
    private int points;

    /** Liên kết đơn hàng */
    private Long orderId;

    /** Mô tả giao dịch: EARN | REDEEM */
    @Column(length = 50)
    private String txType;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
