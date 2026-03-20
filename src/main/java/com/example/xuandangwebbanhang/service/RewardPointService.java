package com.example.xuandangwebbanhang.service;

import com.example.xuandangwebbanhang.model.RewardPoint;
import com.example.xuandangwebbanhang.model.Voucher;
import com.example.xuandangwebbanhang.repository.RewardPointRepository;
import com.example.xuandangwebbanhang.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RewardPointService {

    private final RewardPointRepository rewardPointRepository;
    private final VoucherRepository voucherRepository;

    /** 1 điểm tích từ mỗi 7,500₫ mua hàng */
    public static final double EARN_PER_POINT = 7_500;

    /** Tỷ lệ đổi: 1 điểm = 100₫ (10 điểm = 1,000₫) */
    public static final double REDEEM_VALUE_PER_POINT = 100;

    // ─── CỘNG ĐIỂM ──────────────────────────────────────────────
    /** Cộng điểm sau khi đặt hàng thành công, gắn username để tra cứu chính xác */
    public void addPoints(String username, String userPhone, int points, Long orderId) {
        if (points <= 0) return;
        if ((username == null || username.isBlank()) && (userPhone == null || userPhone.isBlank())) return;

        RewardPoint rp = new RewardPoint();
        rp.setUsername(username != null ? username : "");
        rp.setUserPhone(userPhone != null ? userPhone : "");
        rp.setPoints(points);
        rp.setOrderId(orderId);
        rp.setTxType("EARN");
        rewardPointRepository.save(rp);
    }

    // ─── SỐ DƯ ĐIỂM ─────────────────────────────────────────────
    public int getBalanceByUsername(String username) {
        if (username == null || username.isBlank()) return 0;
        return rewardPointRepository.sumPointsByUsername(username);
    }

    /** Backward-compat – vẫn giữ để OrderService cũ không lỗi */
    public int getBalance(String userPhone) {
        if (userPhone == null || userPhone.isBlank()) return 0;
        return rewardPointRepository.sumPointsByUserPhone(userPhone);
    }

    // ─── ĐỔI ĐIỂM → VOUCHER ─────────────────────────────────────
    /**
     * Đổi điểm thành voucher.
     * @return Voucher vừa tạo, hoặc null nếu không đủ điểm
     */
    public Voucher redeemForVoucher(String username, String userPhone, int pointsToRedeem) {
        if (pointsToRedeem <= 0) return null;

        int balance = getBalanceByUsername(username);
        if (balance < pointsToRedeem) return null;

        double discountAmount = pointsToRedeem * REDEEM_VALUE_PER_POINT;

        // Trừ điểm
        RewardPoint deduct = new RewardPoint();
        deduct.setUsername(username);
        deduct.setUserPhone(userPhone != null ? userPhone : "");
        deduct.setPoints(-pointsToRedeem);
        deduct.setTxType("REDEEM");
        rewardPointRepository.save(deduct);

        // Tạo voucher
        Voucher voucher = new Voucher();
        voucher.setCode("VCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        voucher.setUsername(username);
        voucher.setDiscountAmount(discountAmount);
        voucher.setPointsUsed(pointsToRedeem);
        voucher.setUsed(false);
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setExpiresAt(LocalDateTime.now().plusDays(30));
        return voucherRepository.save(voucher);
    }

    // ─── LỊCH SỬ VOUCHER ─────────────────────────────────────────
    public List<Voucher> getVouchersByUsername(String username) {
        return voucherRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    public Voucher findAvailableVoucher(String username, String code) {
        if (username == null || username.isBlank() || code == null || code.isBlank()) {
            return null;
        }
        return voucherRepository.findByCode(code.trim().toUpperCase())
                .filter(v -> username.equals(v.getUsername()))
                .filter(v -> !v.isUsed())
                .filter(v -> v.getExpiresAt() != null && v.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(null);
    }

    public void markVoucherUsed(String code, String username) {
        if (code == null || code.isBlank()) {
            return;
        }
        voucherRepository.findByCode(code.trim().toUpperCase())
                .filter(v -> username == null || username.isBlank() || username.equals(v.getUsername()))
                .ifPresent(v -> {
                    v.setUsed(true);
                    voucherRepository.save(v);
                });
    }
}
