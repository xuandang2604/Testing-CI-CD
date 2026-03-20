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
@Table(name = "vouchers")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã voucher duy nhất, ví dụ: VCH-A1B2C3D4 */
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    /** Tên đăng nhập của user được cấp */
    @Column(nullable = false)
    private String username;

    /** Số tiền giảm giá (VNĐ) */
    @Column(nullable = false)
    private double discountAmount;

    /** Số điểm đã đổi để tạo voucher này */
    @Column(nullable = false)
    private int pointsUsed;

    /** Đã dùng chưa */
    @Column(nullable = false)
    private boolean used = false;

    /** Thời điểm tạo */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Hết hạn sau 30 ngày */
    @Column(nullable = false)
    private LocalDateTime expiresAt;
}

