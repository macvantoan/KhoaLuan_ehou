package com.phonestore.controller;

import com.phonestore.model.Order;
import com.phonestore.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private OrderRepository orderRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ReviewRepository reviewRepo;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);

        BigDecimal monthRevenue = orderRepo.sumRevenueByPeriod(monthStart, now);
        BigDecimal lastMonthRevenue = orderRepo.sumRevenueByPeriod(lastMonthStart, monthStart);

        long monthOrders = orderRepo.countByPeriod(monthStart, now);
        long newCustomers = userRepo.countByCreatedAtAfter(monthStart);

        long pendingOrders = orderRepo.countByStatus(Order.OrderStatus.PENDING);
        long shippingOrders = orderRepo.countByStatus(Order.OrderStatus.SHIPPING);
        long totalProducts = productRepo.count();
        Long totalSold = productRepo.totalSold();

        double revenueGrowth = 0;
        if (lastMonthRevenue != null && lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            revenueGrowth = (monthRevenue != null ? monthRevenue.doubleValue() : 0) / lastMonthRevenue.doubleValue() * 100 - 100;
        }

        return ResponseEntity.ok(Map.of(
            "monthRevenue", monthRevenue != null ? monthRevenue : BigDecimal.ZERO,
            "revenueGrowth", Math.round(revenueGrowth * 10.0) / 10.0,
            "monthOrders", monthOrders,
            "newCustomers", newCustomers,
            "pendingOrders", pendingOrders,
            "shippingOrders", shippingOrders,
            "totalProducts", totalProducts,
            "totalSold", totalSold != null ? totalSold : 0,
            "lowStockProducts", productRepo.findLowStockProducts().size()
        ));
    }

    @GetMapping("/stats/orders-by-status")
    public ResponseEntity<?> ordersByStatus() {
        return ResponseEntity.ok(Map.of(
            "PENDING", orderRepo.countByStatus(Order.OrderStatus.PENDING),
            "CONFIRMED", orderRepo.countByStatus(Order.OrderStatus.CONFIRMED),
            "SHIPPING", orderRepo.countByStatus(Order.OrderStatus.SHIPPING),
            "DELIVERED", orderRepo.countByStatus(Order.OrderStatus.DELIVERED),
            "CANCELLED", orderRepo.countByStatus(Order.OrderStatus.CANCELLED)
        ));
    }
}
