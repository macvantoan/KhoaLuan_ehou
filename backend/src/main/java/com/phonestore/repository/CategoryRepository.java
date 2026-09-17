package com.phonestore.repository;

import com.phonestore.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();
    List<Category> findAllByOrderBySortOrderAsc();
    boolean existsBySlug(String slug);
}
