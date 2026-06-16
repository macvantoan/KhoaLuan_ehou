package com.phonestore.controller;

import com.phonestore.dto.OrderRequest;
import com.phonestore.model.*;
import com.phonestore.repository.*;
import com.phonestore.security.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired private OrderRepository orderRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private UserRepository userRepo;

    // USER: Get own orders
    @GetMapping("/orders")
    public ResponseEntity<?> getMyOrders(Authentication auth,
                                          @RequestParam(required = false) String status) {
        User user = getUser(auth);
        List<Order> orders = orderRepo.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (status != null) {
            orders = orders.stream().filter(o -> o.getStatus().name().equals(status)).toList();
        }
        return ResponseEntity.ok(orders);
    }

    // USER: Get order detail
    @GetMapping("/orders/{code}")
    public ResponseEntity<?> getOrderDetail(@PathVariable String code, Authentication auth) {
        User user = getUser(auth);
        Optional<Order> order = orderRepo.findByOrderCode(code);
        if (order.isEmpty()) return ResponseEntity.notFound().build();
        if (!order.get().getUser().getId().equals(user.getId()) && !user.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(order.get());
    }

    // USER: Place order
    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@Valid @RequestBody OrderRequest req, Authentication auth) {
        User user = getUser(auth);

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderRequest.OrderItemRequest itemReq : req.getItems()) {
            Product p = productRepo.findById(itemReq.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.getProductId()));
            if (p.getStock() < itemReq.getQuantity()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Sản phẩm " + p.getName() + " không đủ hàng"));
            }
            BigDecimal itemTotal = p.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(itemTotal);

            items.add(OrderItem.builder()
                .product(p)
                .productName(p.getName())
                .productImage(p.getImages().isEmpty() ? null : p.getImages().get(0))
                .quantity(itemReq.getQuantity())
                .unitPrice(p.getPrice())
                .totalPrice(itemTotal)
                .build());
        }

        BigDecimal shippingFee = calculateShipping(req.getShippingMethod(), subtotal);
        BigDecimal total = subtotal.add(shippingFee);

        String code = "PS" + System.currentTimeMillis();
        Order order = Order.builder()
            .orderCode(code)
            .user(user)
            .recipientName(req.getRecipientName())
            .recipientPhone(req.getRecipientPhone())
            .recipientEmail(req.getRecipientEmail())
            .shippingAddress(req.getShippingAddress())
            .province(req.getProvince())
            .district(req.getDistrict())
            .ward(req.getWard())
            .note(req.getNote())
            .subtotal(subtotal)
            .shippingFee(shippingFee)
            .totalAmount(total)
            .shippingMethod(req.getShippingMethod() != null ? req.getShippingMethod() : Order.ShippingMethod.STANDARD)
            .paymentMethod(req.getPaymentMethod())
            .status(Order.OrderStatus.PENDING)
            .build();

        order.setItems(items);
        items.forEach(i -> i.setOrder(order));

        Order saved = orderRepo.save(order);

        // Decrease stock
        for (OrderRequest.OrderItemRequest itemReq : req.getItems()) {
            productRepo.decreaseStock(itemReq.getProductId(), itemReq.getQuantity());
        }

        return ResponseEntity.ok(Map.of("orderCode", saved.getOrderCode(), "message", "Đặt hàng thành công!"));
    }

    // USER: Cancel order
    @PatchMapping("/orders/{code}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String code,
                                          @RequestBody(required = false) Map<String, String> body,
                                          Authentication auth) {
        User user = getUser(auth);
        Optional<Order> optOrder = orderRepo.findByOrderCode(code);
        if (optOrder.isEmpty()) return ResponseEntity.notFound().build();

        Order order = optOrder.get();
        if (!order.getUser().getId().equals(user.getId())) return ResponseEntity.status(403).build();
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không thể hủy đơn hàng ở trạng thái này"));
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledReason(body != null ? body.get("reason") : null);
        orderRepo.save(order);
        return ResponseEntity.ok(Map.of("message", "Đã hủy đơn hàng"));
    }

    // ADMIN: Get all orders
    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllOrders(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String payment,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Order.OrderStatus orderStatus = status != null ? Order.OrderStatus.valueOf(status) : null;
        Order.PaymentMethod paymentMethod = payment != null ? Order.PaymentMethod.valueOf(payment) : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepo.search(q, orderStatus, paymentMethod, null, null, pageable);
        return ResponseEntity.ok(Map.of(
            "content", orders.getContent(),
            "totalElements", orders.getTotalElements(),
            "totalPages", orders.getTotalPages()
        ));
    }

    // ADMIN: Update order status
    @PatchMapping("/admin/orders/{code}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable String code, @RequestBody Map<String, String> body) {
        Optional<Order> optOrder = orderRepo.findByOrderCode(code);
        if (optOrder.isEmpty()) return ResponseEntity.notFound().build();

        Order order = optOrder.get();
        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(body.get("status"));
        order.setStatus(newStatus);

        LocalDateTime now = LocalDateTime.now();
        if (newStatus == Order.OrderStatus.CONFIRMED) order.setConfirmedAt(now);
        else if (newStatus == Order.OrderStatus.SHIPPING) order.setShippedAt(now);
        else if (newStatus == Order.OrderStatus.DELIVERED) order.setDeliveredAt(now);

        orderRepo.save(order);
        return ResponseEntity.ok(Map.of("message", "Đã cập nhật trạng thái"));
    }

    private BigDecimal calculateShipping(Order.ShippingMethod method, BigDecimal subtotal) {
        if (subtotal.compareTo(BigDecimal.valueOf(2_000_000)) >= 0) return BigDecimal.ZERO;
        if (method == Order.ShippingMethod.EXPRESS) return BigDecimal.valueOf(30_000);
        if (method == Order.ShippingMethod.SAME_DAY) return BigDecimal.valueOf(60_000);
        return BigDecimal.ZERO;
    }

    private User getUser(Authentication auth) {
        return userRepo.findByUsernameOrEmail(auth.getName(), auth.getName()).orElseThrow();
    }
}
