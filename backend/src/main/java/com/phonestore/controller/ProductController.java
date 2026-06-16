package com.phonestore.controller;

import com.phonestore.model.Product;
import com.phonestore.repository.ProductRepository;
import com.phonestore.repository.ReviewRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired private ProductRepository productRepo;
    @Autowired private ReviewRepository reviewRepo;

    @GetMapping
    public ResponseEntity<?> getAll(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String brand,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) String ram,
        @RequestParam(required = false) String sort,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size
    ) {
        Sort sorting = switch (sort == null ? "newest" : sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "best_seller" -> Sort.by("soldCount").descending();
            case "rating" -> Sort.by("avgRating").descending();
            default -> Sort.by("createdAt").descending();
        };

        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Product> products = productRepo.search(q, brand, minPrice, maxPrice, ram, Product.Status.ACTIVE, pageable);

        return ResponseEntity.ok(Map.of(
            "content", products.getContent(),
            "totalElements", products.getTotalElements(),
            "totalPages", products.getTotalPages(),
            "currentPage", page
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return productRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<?> getRelated(@PathVariable Long id) {
        Product p = productRepo.findById(id).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        List<Product> related = productRepo.findByBrandAndIdNot(p.getBrand(), id, PageRequest.of(0, 6));
        return ResponseEntity.ok(related);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody Product product) {
        if (product.getSku() != null && productRepo.existsBySku(product.getSku())) {
            return ResponseEntity.badRequest().body(Map.of("message", "SKU đã tồn tại"));
        }
        product.setId(null);
        return ResponseEntity.ok(productRepo.save(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Product product) {
        return productRepo.findById(id).map(existing -> {
            product.setId(id);
            product.setCreatedAt(existing.getCreatedAt());
            product.setSoldCount(existing.getSoldCount());
            product.setAvgRating(existing.getAvgRating());
            product.setReviewCount(existing.getReviewCount());
            return ResponseEntity.ok(productRepo.save(product));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!productRepo.existsById(id)) return ResponseEntity.notFound().build();
        productRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa sản phẩm"));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return productRepo.findById(id).map(p -> {
            p.setStock(body.get("stock"));
            return ResponseEntity.ok(productRepo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getLowStock() {
        return ResponseEntity.ok(productRepo.findLowStockProducts());
    }
}
