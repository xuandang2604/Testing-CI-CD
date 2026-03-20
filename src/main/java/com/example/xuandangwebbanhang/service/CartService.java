package com.example.xuandangwebbanhang.service;

import com.example.xuandangwebbanhang.model.CartItem;
import com.example.xuandangwebbanhang.model.Product;
import com.example.xuandangwebbanhang.model.Voucher;
import com.example.xuandangwebbanhang.repository.ProductRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;

@Service
@SessionScope
public class CartService {
    private static final double FREE_SHIP_MIN_TOTAL = 1_000_000;
    private static final int FREE_SHIP_MIN_QUANTITY = 2;
    private static final double DEFAULT_SHIPPING_FEE = 30_001;
    private static final int POINTS_PER_REWARD_BLOCK = 1;
    private static final double REWARD_VALUE_PER_BLOCK = 7_500;

    private final List<CartItem> cartItems = new ArrayList<>();
    private PendingCheckoutInfo pendingCheckoutInfo;

    private String appliedVoucherCode;
    private double appliedVoucherDiscount;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RewardPointService rewardPointService;

    public void addToCart(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        int safeQuantity = Math.max(quantity, 1);
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                item.setProduct(product); // refresh latest product info
                item.setQuantity(item.getQuantity() + safeQuantity);
                return;
            }
        }

        cartItems.add(new CartItem(product, safeQuantity));
    }

    public void updateQuantity(Long productId, int quantity) {
        if (quantity <= 0) {
            removeFromCart(productId);
            return;
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                item.setProduct(product);
                item.setQuantity(quantity);
                return;
            }
        }
        cartItems.add(new CartItem(product, quantity));
    }

    public List<CartItem> getCartItems() {
        refreshProductsFromDb();
        return cartItems;
    }

    public int getTotalItems() {
        refreshProductsFromDb();
        return cartItems.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public double getSubTotalAmount() {
        refreshProductsFromDb();
        return cartItems.stream().mapToDouble(this::getCartItemSubtotal).sum();
    }

    public double getShippingFee() {
        double subTotal = getSubTotalAmount();
        int totalItems = getTotalItems();
        return (subTotal >= FREE_SHIP_MIN_TOTAL && totalItems >= FREE_SHIP_MIN_QUANTITY) ? 0 : DEFAULT_SHIPPING_FEE;
    }

    public int getRewardPoints() {
        double subTotal = getSubTotalAmount();
        int rewardBlocks = (int) Math.floor(subTotal / REWARD_VALUE_PER_BLOCK);
        return rewardBlocks * POINTS_PER_REWARD_BLOCK;
    }

    public double getRewardDiscount() {
        // Điểm chỉ tích lũy, không tự trừ đơn hiện tại
        return 0;
    }

    public String getAppliedVoucherCode() {
        return appliedVoucherCode;
    }

    public double getAppliedVoucherDiscount() {
        return appliedVoucherDiscount;
    }

    public String applyVoucher(String username, String voucherCode) {
        if (voucherCode == null || voucherCode.isBlank()) {
            return "Vui long nhap ma voucher";
        }
        Voucher voucher = rewardPointService.findAvailableVoucher(username, voucherCode);
        if (voucher == null) {
            return "Ma voucher khong hop le, da dung hoac het han";
        }
        this.appliedVoucherCode = voucher.getCode();
        this.appliedVoucherDiscount = Math.max(voucher.getDiscountAmount(), 0);
        return null;
    }

    public void removeVoucher() {
        this.appliedVoucherCode = null;
        this.appliedVoucherDiscount = 0;
    }

    public double getTotalAmount() {
        double rawTotal = getSubTotalAmount() + getShippingFee() - getRewardDiscount() - getAppliedVoucherDiscount();
        return Math.max(rawTotal, 0);
    }

    public double getCartItemSubtotal(CartItem item) {
        Product product = item.getProduct();
        int quantity = Math.max(item.getQuantity(), 0);
        if (!isDiscountPromotion(product)) {
            return product.getPrice() * quantity;
        }

        int discountedQty = getAppliedPromotionQuantity(item);
        int regularQty = Math.max(quantity - discountedQty, 0);
        double discountedUnitPrice = getDiscountedUnitPrice(product);

        return (discountedUnitPrice * discountedQty) + (product.getPrice() * regularQty);
    }

    public int getAppliedPromotionQuantity(CartItem item) {
        refreshSingleProduct(item);
        Product product = item.getProduct();
        if (!isDiscountPromotion(product)) {
            return 0;
        }

        int promotionStock = product.getPromotionStockQuantity() == null ? 0 : product.getPromotionStockQuantity();
        return Math.min(Math.max(item.getQuantity(), 0), Math.max(promotionStock, 0));
    }

    public int getRegularPriceQuantity(CartItem item) {
        int totalQty = Math.max(item.getQuantity(), 0);
        return Math.max(totalQty - getAppliedPromotionQuantity(item), 0);
    }

    public double getDiscountedUnitPrice(Product product) {
        if (!isDiscountPromotion(product)) {
            return product.getPrice();
        }
        double discountPercent = product.getDiscountPercent() == null ? 0.0 : product.getDiscountPercent();
        return product.getPrice() * (1 - discountPercent / 100.0);
    }

    public void savePendingCheckoutInfo(String customerName,
                                        String phoneNumber,
                                        String address,
                                        String note,
                                        String paymentMethod,
                                        String momoOrderId) {
        PendingCheckoutInfo info = new PendingCheckoutInfo();
        info.setCustomerName(customerName);
        info.setPhoneNumber(phoneNumber);
        info.setAddress(address);
        info.setNote(note);
        info.setPaymentMethod(paymentMethod);
        info.setMomoOrderId(momoOrderId);
        info.setCartSnapshot(createCartSnapshot());
        info.setAppliedVoucherCode(this.appliedVoucherCode);
        info.setAppliedVoucherDiscount(this.appliedVoucherDiscount);
        this.pendingCheckoutInfo = info;
    }

    public void restoreVoucherFromPending(PendingCheckoutInfo info) {
        if (info == null) {
            return;
        }
        this.appliedVoucherCode = info.getAppliedVoucherCode();
        this.appliedVoucherDiscount = Math.max(info.getAppliedVoucherDiscount(), 0);
    }

    public PendingCheckoutInfo getPendingCheckoutInfo() {
        return pendingCheckoutInfo;
    }

    public void clearPendingCheckoutInfo() {
        this.pendingCheckoutInfo = null;
    }

    public List<CartItem> createCartSnapshot() {
        List<CartItem> snapshot = new ArrayList<>();
        for (CartItem item : cartItems) {
            snapshot.add(new CartItem(item.getProduct(), item.getQuantity()));
        }
        return snapshot;
    }

    public void removeFromCart(Long productId) {
        cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void clearCart() {
        cartItems.clear();
        removeVoucher();
    }

    private boolean isDiscountPromotion(Product product) {
        return "DISCOUNT".equalsIgnoreCase(product.getPromotionType())
                && product.getDiscountPercent() != null
                && product.getDiscountPercent() > 0;
    }

    private void refreshProductsFromDb() {
        for (CartItem item : cartItems) {
            refreshSingleProduct(item);
        }
    }

    private void refreshSingleProduct(CartItem item) {
        productRepository.findById(item.getProduct().getId()).ifPresent(item::setProduct);
    }

    @Getter
    @Setter
    public static class PendingCheckoutInfo {
        private String customerName;
        private String phoneNumber;
        private String address;
        private String note;
        private String paymentMethod;
        private String momoOrderId;
        private List<CartItem> cartSnapshot;
        private String appliedVoucherCode;
        private double appliedVoucherDiscount;
    }
}
