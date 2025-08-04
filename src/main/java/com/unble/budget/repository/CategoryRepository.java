package com.unble.budget.repository;

import com.unble.budget.entity.Category;
import com.unble.budget.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // 기존 메서드들 (sortOrder 적용)
    @Query("SELECT c FROM Category c WHERE (c.user = :user OR c.isDefault = true) " +
           "ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findByUserOrIsDefaultTrueOrderBySortOrder(@Param("user") User user);
    
    @Query("SELECT c FROM Category c WHERE c.isDefault = true ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findByIsDefaultTrueOrderBySortOrder();
    
    // 기존 호환성을 위한 메서드들
    List<Category> findByUserOrIsDefaultTrueOrderByName(User user);
    List<Category> findByIsDefaultTrueOrderByName();
    
    // 카테고리 타입별 조회
    @Query("SELECT c FROM Category c WHERE (c.user = :user OR c.isDefault = true) " +
           "AND c.categoryType = :categoryType ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findByUserAndCategoryTypeOrderBySortOrder(@Param("user") User user, 
                                                             @Param("categoryType") Category.CategoryType categoryType);
    
    @Query("SELECT c FROM Category c WHERE c.isDefault = true AND c.categoryType = :categoryType " +
           "ORDER BY c.sortOrder ASC, c.name ASC")
    List<Category> findByIsDefaultTrueAndCategoryTypeOrderBySortOrder(@Param("categoryType") Category.CategoryType categoryType);
    
    // 사용자별 카테고리
    List<Category> findByUserOrderBySortOrderAscNameAsc(User user);
    
    // 카테고리 타입별 개수
    long countByCategoryType(Category.CategoryType categoryType);
    long countByIsDefaultTrueAndCategoryType(Category.CategoryType categoryType);
    
    // 이름으로 카테고리 찾기
    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.user IS NULL OR c.isDefault = true)")
    java.util.Optional<Category> findByName(@Param("name") String name);
}