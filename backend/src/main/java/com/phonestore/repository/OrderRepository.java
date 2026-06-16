package com.phonestore.repository;

import com.phonestore.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT o FROM Order o WHERE " +
           "(:q IS NULL OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(o.recipientName) LIKE LOWER(CONCAT('%',:q,'%'))) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:payment IS NULL OR o.paymentMethod = :payment) AND " +
           "(:fromDate IS NULL OR o.createdAt >= :fromDate) AND " +
           "(:toDate IS NULL OR o.createdAt <= :toDate)")
    Page<Order> search(@Param("q") String q,
                       @Param("status") Order.OrderStatus status,
                       @Param("payment") Order.PaymentMethod payment,
                       @Param("fromDate") LocalDateTime fromDate,
                       @Param("toDate") LocalDateTime toDate,
                       Pageable pageable);

    long countByStatus(Order.OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt BETWEEN :from AND :to")
    BigDecimal sumRevenueByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    Long countByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
