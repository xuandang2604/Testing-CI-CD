package com.example.xuandangwebbanhang.service;

import com.example.xuandangwebbanhang.model.CartItem;
import com.example.xuandangwebbanhang.model.Order;
import com.example.xuandangwebbanhang.model.OrderDetail;
import com.example.xuandangwebbanhang.model.Product;
import com.example.xuandangwebbanhang.repository.OrderDetailRepository;
import com.example.xuandangwebbanhang.repository.OrderRepository;
import com.example.xuandangwebbanhang.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private RewardPointService rewardPointService;

    @Transactional
    public Order createOrder(String customerName, String phoneNumber, String address,
                             String note, String paymentMethod, List<CartItem> cartItems) {
        return createOrderInternal(customerName, phoneNumber, address, note, paymentMethod,
                "UNPAID", null, cartItems);
    }

    @Transactional
    public Order createPaidMomoOrder(String customerName, String phoneNumber, String address,
                                     String note, String momoOrderId, List<CartItem> cartItems) {
        return createOrderInternal(customerName, phoneNumber, address, note, "MOMO",
                "PAID", momoOrderId, cartItems);
    }

    @Transactional
    public Order createOrder(String customerName, List<CartItem> cartItems) {
        return createOrderInternal(customerName, null, null, null, "COD", "UNPAID", null, cartItems);
    }

    private Order createOrderInternal(String customerName,
                                      String phoneNumber,
                                      String address,
                                      String note,
                                      String paymentMethod,
                                      String paymentStatus,
                                      String momoOrderId,
                                      List<CartItem> cartItems) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setPhoneNumber(phoneNumber);
        order.setAddress(address);
        order.setNote(note);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(paymentStatus);
        order.setMomoOrderId(momoOrderId);

        order.setSubTotal(cartService.getSubTotalAmount());
        order.setShippingFee(cartService.getShippingFee());
        order.setRewardPoints(cartService.getRewardPoints());
        order.setRewardDiscount(cartService.getRewardDiscount());
        order.setVoucherCode(cartService.getAppliedVoucherCode());
        order.setVoucherDiscount(cartService.getAppliedVoucherDiscount());
        order.setTotalAmount(cartService.getTotalAmount());

        order = orderRepository.save(order);

        for (CartItem item : cartItems) {
            Product latestProduct = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProduct().getId()));

            consumePromotionStock(latestProduct, item.getQuantity());
            productRepository.save(latestProduct);

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(latestProduct);
            detail.setQuantity(item.getQuantity());
            orderDetailRepository.save(detail);
        }

        // Lấy username từ Security context (luôn có cho cả COD và MOMO)
        String username = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            username = auth.getName();
        }

        // Cộng điểm cho cả COD (UNPAID) và MOMO (PAID)
        int pts = order.getRewardPoints();
        if (pts > 0) {
            rewardPointService.addPoints(username, phoneNumber, pts, order.getId());
        }

        // Nếu có voucher thì đánh dấu đã dùng sau khi đơn tạo thành công
        if (order.getVoucherCode() != null && !order.getVoucherCode().isBlank()) {
            rewardPointService.markVoucherUsed(order.getVoucherCode(), username);
        }

        cartService.clearCart();
        return order;
    }

    private void consumePromotionStock(Product product, int orderedQuantity) {
        String promotionType = product.getPromotionType();
        if (promotionType == null || "NONE".equalsIgnoreCase(promotionType)) {
            return;
        }

        int currentPromotionStock = product.getPromotionStockQuantity() == null
                ? 0 : product.getPromotionStockQuantity();
        int usedPromotionQuantity = Math.min(Math.max(orderedQuantity, 0), Math.max(currentPromotionStock, 0));
        product.setPromotionStockQuantity(Math.max(currentPromotionStock - usedPromotionQuantity, 0));
    }
}
