package com.example.xuandangwebbanhang.controller;

import com.example.xuandangwebbanhang.model.CartItem;
import com.example.xuandangwebbanhang.service.CartService;
import com.example.xuandangwebbanhang.service.MomoService;
import com.example.xuandangwebbanhang.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private CartService cartService;
    @Autowired
    private MomoService momoService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("cartSubTotal", cartService.getSubTotalAmount());
        model.addAttribute("shippingFee", cartService.getShippingFee());
        model.addAttribute("rewardPoints", cartService.getRewardPoints());
        model.addAttribute("rewardDiscount", cartService.getRewardDiscount());
        model.addAttribute("voucherCode", cartService.getAppliedVoucherCode());
        model.addAttribute("voucherDiscount", cartService.getAppliedVoucherDiscount());
        model.addAttribute("cartTotalAmount", cartService.getTotalAmount());
        return "cart/checkout";
    }

    @PostMapping("/checkout/apply-voucher")
    public String applyVoucher(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam String voucherCode,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Vui long dang nhap de dung voucher");
            return "redirect:/login";
        }
        String message = cartService.applyVoucher(userDetails.getUsername(), voucherCode);
        if (message != null) {
            redirectAttributes.addFlashAttribute("error", message);
        } else {
            redirectAttributes.addFlashAttribute("success", "Ap dung voucher thanh cong: " + cartService.getAppliedVoucherCode());
        }
        return "redirect:/order/checkout";
    }

    @PostMapping("/checkout/remove-voucher")
    public String removeVoucher(RedirectAttributes redirectAttributes) {
        cartService.removeVoucher();
        redirectAttributes.addFlashAttribute("success", "Da go voucher khoi don hang");
        return "redirect:/order/checkout";
    }

    @PostMapping("/submit")
    public String submitOrder(@RequestParam String customerName,
                              @RequestParam String phoneNumber,
                              @RequestParam String address,
                              @RequestParam(required = false) String note,
                              @RequestParam(defaultValue = "COD") String paymentMethod,
                              RedirectAttributes redirectAttributes) {

        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        if ("MOMO".equalsIgnoreCase(paymentMethod)) {
            try {
                String momoResponse = momoService.createPaymentRequest(String.valueOf((long) cartService.getTotalAmount()));
                JsonNode root = objectMapper.readTree(momoResponse);
                String payUrl = root.path("payUrl").asText("");
                String momoOrderId = root.path("orderId").asText("");

                if (payUrl == null || payUrl.isBlank()) {
                    redirectAttributes.addFlashAttribute("error", "Khong tao duoc link thanh toan MoMo.");
                    return "redirect:/order/checkout";
                }

                cartService.savePendingCheckoutInfo(customerName, phoneNumber, address, note, paymentMethod, momoOrderId);
                return "redirect:" + payUrl;
            } catch (Exception ex) {
                redirectAttributes.addFlashAttribute("error", "Loi ket noi MoMo: " + ex.getMessage());
                return "redirect:/order/checkout";
            }
        }

        orderService.createOrder(customerName, phoneNumber, address, note, paymentMethod, cartItems);
        return "redirect:/order/confirmation";
    }

    @GetMapping("/momo-return")
    public String momoReturn(@RequestParam(required = false) String resultCode,
                             @RequestParam(required = false) String orderId,
                             RedirectAttributes redirectAttributes) {
        if (!"0".equals(resultCode)) {
            redirectAttributes.addFlashAttribute("error", "Thanh toan MoMo that bai hoac bi huy.");
            return "redirect:/order/checkout";
        }

        CartService.PendingCheckoutInfo pending = cartService.getPendingCheckoutInfo();
        if (pending == null || pending.getCartSnapshot() == null || pending.getCartSnapshot().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Khong tim thay thong tin don hang cho giao dich MoMo.");
            return "redirect:/cart";
        }

        cartService.restoreVoucherFromPending(pending);

        String momoOrderId = orderId != null && !orderId.isBlank() ? orderId : pending.getMomoOrderId();
        orderService.createPaidMomoOrder(
                pending.getCustomerName(),
                pending.getPhoneNumber(),
                pending.getAddress(),
                pending.getNote(),
                momoOrderId,
                pending.getCartSnapshot()
        );

        cartService.clearPendingCheckoutInfo();
        return "redirect:/order/confirmation";
    }

    @GetMapping("/confirmation")
    public String orderConfirmation(Model model) {
        model.addAttribute("message", "Your order has been successfully placed.");
        return "cart/order-confirmation";
    }
}