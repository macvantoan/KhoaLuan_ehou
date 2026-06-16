package com.phonestore.repository;

import com.phonestore.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p WHERE " +
           "(:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:q,'%'))) AND " +
           "(:brand IS NULL OR p.brand = :brand) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:ram IS NULL OR p.ram = :ram) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<Product> search(@Param("q") String q,
                         @Param("brand") String brand,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         @Param("ram") String ram,
                         @Param("status") Product.Status status,
                         Pageable pageable);

    List<Product> findByBrandAndIdNot(String brand, Long id, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stock <= p.lowStockThreshold ORDER BY p.stock ASC")
    List<Product> findLowStockProducts();

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.stock = p.stock - :qty, p.soldCount = p.soldCount + :qty WHERE p.id = :id AND p.stock >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty);

    @Query("SELECT SUM(p.soldCount) FROM Product p")
    Long totalSold();
}
