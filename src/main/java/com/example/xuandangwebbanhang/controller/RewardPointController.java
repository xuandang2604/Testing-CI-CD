package com.example.xuandangwebbanhang.controller;

import com.example.xuandangwebbanhang.model.User;
import com.example.xuandangwebbanhang.model.Voucher;
import com.example.xuandangwebbanhang.service.EmailService;
import com.example.xuandangwebbanhang.service.RewardPointService;
import com.example.xuandangwebbanhang.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Controller
@RequestMapping("/rewards")
@RequiredArgsConstructor
public class RewardPointController {

    private static final String OTP_CODE_KEY = "rewardOtpCode";
    private static final String OTP_EXPIRE_KEY = "rewardOtpExpire";
    private static final String OTP_EMAIL_KEY = "rewardOtpEmail";

    private final RewardPointService rewardPointService;
    private final UserService userService;
    private final EmailService emailService;

    /**
     * Trang đổi điểm tích lũy – chỉ USER mới vào được (SecurityConfig đảm bảo).
     */
    @GetMapping
    public String rewardsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String username = user.getUsername();
        int balance = rewardPointService.getBalanceByUsername(username);
        List<Voucher> vouchers = rewardPointService.getVouchersByUsername(username);

        model.addAttribute("user", user);
        model.addAttribute("balance", balance);
        model.addAttribute("redeemValuePerPoint", RewardPointService.REDEEM_VALUE_PER_POINT);
        model.addAttribute("totalValue", balance * RewardPointService.REDEEM_VALUE_PER_POINT);
        model.addAttribute("vouchers", vouchers);
        return "user/rewards";
    }

    /**
     * Gửi OTP xác nhận qua email.
     */
    @PostMapping("/send-otp")
    public String sendOtp(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam String confirmEmail,
                          @RequestParam(required = false) Integer pointsToRedeem,
                          @RequestParam(required = false) String otpCode,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        addRedeemPrefill(redirectAttributes, pointsToRedeem, confirmEmail, otpCode);

        if (confirmEmail == null || !confirmEmail.trim().equalsIgnoreCase(user.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "Email nhập vào không khớp với tài khoản đang nhập.");
            return "redirect:/rewards";
        }

        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        long expireAt = Instant.now().plusSeconds(5 * 60).toEpochMilli();

        try {
            emailService.sendRewardOtp(user.getEmail(), otp);
            session.setAttribute(OTP_CODE_KEY, otp);
            session.setAttribute(OTP_EXPIRE_KEY, expireAt);
            session.setAttribute(OTP_EMAIL_KEY, user.getEmail().toLowerCase());
            redirectAttributes.addFlashAttribute("success", "Đã gửi mã OTP về email. Mã có hiệu lực 5 phút.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Không gửi được OTP qua email: " + ex.getMessage());
        }
        return "redirect:/rewards";
    }

    /**
     * Xử lý đổi điểm – yêu cầu xác thực email và OTP.
     */
    @PostMapping("/redeem")
    public String redeem(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam int pointsToRedeem,
                         @RequestParam String confirmEmail,
                         @RequestParam String otpCode,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {

        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        addRedeemPrefill(redirectAttributes, pointsToRedeem, confirmEmail, otpCode);

        if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(confirmEmail.trim())) {
            redirectAttributes.addFlashAttribute("error", "Email xác thực không đúng.");
            return "redirect:/rewards";
        }
        if (pointsToRedeem <= 0) {
            redirectAttributes.addFlashAttribute("error", "Số điểm đổi phải lớn hơn 0.");
            return "redirect:/rewards";
        }

        String savedOtp = (String) session.getAttribute(OTP_CODE_KEY);
        Long expireAt = (Long) session.getAttribute(OTP_EXPIRE_KEY);
        String otpEmail = (String) session.getAttribute(OTP_EMAIL_KEY);

        if (savedOtp == null || expireAt == null || otpEmail == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa gửi OTP. Vui lòng bấm 'Gửi mã OTP' trước.");
            return "redirect:/rewards";
        }
        if (Instant.now().toEpochMilli() > expireAt) {
            redirectAttributes.addFlashAttribute("error", "Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.");
            return "redirect:/rewards";
        }
        if (!otpEmail.equalsIgnoreCase(confirmEmail.trim()) || !savedOtp.equals(otpCode == null ? "" : otpCode.trim())) {
            redirectAttributes.addFlashAttribute("error", "Mã OTP không chính xác.");
            return "redirect:/rewards";
        }

        Voucher voucher = rewardPointService.redeemForVoucher(
                user.getUsername(), user.getPhone(), pointsToRedeem);

        if (voucher == null) {
            int balance = rewardPointService.getBalanceByUsername(user.getUsername());
            redirectAttributes.addFlashAttribute("error",
                    "Không đủ điểm để đổi. Số điểm hiện có: " + balance);
            return "redirect:/rewards";
        }

        // OTP chỉ dùng 1 lần
        session.removeAttribute(OTP_CODE_KEY);
        session.removeAttribute(OTP_EXPIRE_KEY);
        session.removeAttribute(OTP_EMAIL_KEY);

        // Đổi thành công thì clear prefill để form sạch
        redirectAttributes.addFlashAttribute("prefillPoints", null);
        redirectAttributes.addFlashAttribute("prefillEmail", null);
        redirectAttributes.addFlashAttribute("prefillOtp", null);

        String formatted = String.format("%,.0f", voucher.getDiscountAmount()).replace(",", ".");
        redirectAttributes.addFlashAttribute("success",
                "Đổi thành công " + pointsToRedeem + " điểm! Mã voucher của bạn: "
                        + voucher.getCode() + " (giảm " + formatted + "₫, hiệu lực 30 ngày)");
        return "redirect:/rewards";
    }

    private void addRedeemPrefill(RedirectAttributes redirectAttributes,
                                  Integer pointsToRedeem,
                                  String confirmEmail,
                                  String otpCode) {
        if (pointsToRedeem != null && pointsToRedeem > 0) {
            redirectAttributes.addFlashAttribute("prefillPoints", pointsToRedeem);
        }
        if (confirmEmail != null) {
            redirectAttributes.addFlashAttribute("prefillEmail", confirmEmail);
        }
        if (otpCode != null) {
            redirectAttributes.addFlashAttribute("prefillOtp", otpCode);
        }
    }
}
