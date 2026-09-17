package com.phonestore.controller;

import com.phonestore.model.Category;
import com.phonestore.repository.CategoryRepository;
import com.phonestore.repository.ProductRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired private CategoryRepository categoryRepo;
    @Autowired private ProductRepository productRepo;

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) Boolean all) {
        List<Category> categories = Boolean.TRUE.equals(all)
            ? categoryRepo.findAllByOrderBySortOrderAsc()
            : categoryRepo.findByIsActiveTrueOrderBySortOrderAsc();

        List<Map<String, Object>> result = categories.stream().map(c -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("slug", c.getSlug());
            m.put("icon", c.getIcon());
            m.put("description", c.getDescription());
            m.put("sortOrder", c.getSortOrder());
            m.put("isActive", c.getIsActive());
            m.put("productCount", productRepo.countByCategoryId(c.getId()));
            return m;
        }).toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return categoryRepo.findById(id).<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody CategoryRequest req) {
        String slug = req.getSlug() != null && !req.getSlug().isBlank() ? slugify(req.getSlug()) : slugify(req.getName());
        if (categoryRepo.existsBySlug(slug)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Slug đã tồn tại, vui lòng chọn tên khác"));
        }
        Category category = Category.builder()
            .name(req.getName())
            .slug(slug)
            .icon(req.getIcon())
            .description(req.getDescription())
            .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
            .isActive(req.getIsActive() != null ? req.getIsActive() : true)
            .build();
        return ResponseEntity.ok(categoryRepo.save(category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        return categoryRepo.findById(id).<ResponseEntity<?>>map(existing -> {
            String slug = req.getSlug() != null && !req.getSlug().isBlank() ? slugify(req.getSlug()) : slugify(req.getName());
            if (!slug.equals(existing.getSlug()) && categoryRepo.existsBySlug(slug)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Slug đã tồn tại, vui lòng chọn tên khác"));
            }
            existing.setName(req.getName());
            existing.setSlug(slug);
            existing.setIcon(req.getIcon());
            existing.setDescription(req.getDescription());
            if (req.getSortOrder() != null) existing.setSortOrder(req.getSortOrder());
            if (req.getIsActive() != null) existing.setIsActive(req.getIsActive());
            return ResponseEntity.ok(categoryRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!categoryRepo.existsById(id)) return ResponseEntity.notFound().build();
        long productCount = productRepo.countByCategoryId(id);
        if (productCount > 0) {
            return ResponseEntity.badRequest().body(Map.of("message",
                "Không thể xóa danh mục đang có " + productCount + " sản phẩm"));
        }
        categoryRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa danh mục"));
    }

    private String slugify(String input) {
        String noAccent = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noAccent.toLowerCase()
            .replace('đ', 'd')
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
    }

    @Data
    public static class CategoryRequest {
        @NotBlank
        private String name;
        private String slug;
        private String icon;
        private String description;
        private Integer sortOrder;
        private Boolean isActive;
    }
}
