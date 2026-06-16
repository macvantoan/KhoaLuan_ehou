package com.phonestore.controller;

import com.phonestore.model.Review;
import com.phonestore.model.User;
import com.phonestore.repository.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {

    @Autowired private ReviewRepository reviewRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private UserRepository userRepo;

    @GetMapping
    public ResponseEntity<?> getReviews(@PathVariable Long productId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        var reviews = reviewRepo.findByProductIdOrderByCreatedAtDesc(
            productId, PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(Map.of(
            "content", reviews.getContent(),
            "totalElements", reviews.getTotalElements(),
            "avgRating", reviewRepo.getAvgRatingByProductId(productId)
        ));
    }

    @PostMapping
    public ResponseEntity<?> addReview(@PathVariable Long productId,
                                        @RequestBody Map<String, Object> body,
                                        Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();

        User user = userRepo.findByUsernameOrEmail(auth.getName(), auth.getName()).orElseThrow();

        if (reviewRepo.existsByProductIdAndUserId(productId, user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bạn đã đánh giá sản phẩm này rồi"));
        }

        var product = productRepo.findById(productId).orElseThrow();
        int rating = Integer.parseInt(body.get("rating").toString());
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().body(Map.of("message", "Rating phải từ 1-5"));
        }

        Review review = Review.builder()
            .product(product)
            .user(user)
            .rating(rating)
            .title((String) body.get("title"))
            .body((String) body.get("body"))
            .build();

        reviewRepo.save(review);

        // Update product avg rating
        Double avg = reviewRepo.getAvgRatingByProductId(productId);
        long count = reviewRepo.countByProductId(productId);
        product.setAvgRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        product.setReviewCount((int) count);
        productRepo.save(product);

        return ResponseEntity.ok(Map.of("message", "Cảm ơn bạn đã đánh giá!"));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long productId,
                                           @PathVariable Long reviewId,
                                           Authentication auth) {
        User user = userRepo.findByUsernameOrEmail(auth.getName(), auth.getName()).orElseThrow();
        var review = reviewRepo.findById(reviewId).orElse(null);
        if (review == null) return ResponseEntity.notFound().build();
        if (!review.getUser().getId().equals(user.getId()) && user.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        reviewRepo.deleteById(reviewId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa đánh giá"));
    }
}
