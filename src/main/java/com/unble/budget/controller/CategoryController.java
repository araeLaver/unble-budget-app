package com.unble.budget.controller;

import com.unble.budget.dto.CategoryResponse;
import com.unble.budget.entity.Category;
import com.unble.budget.entity.Category.CategoryType;
import com.unble.budget.entity.User;
import com.unble.budget.repository.CategoryRepository;
import com.unble.budget.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryController(CategoryRepository categoryRepository, 
                             UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getUserCategories(Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            List<CategoryResponse> categories = categoryRepository
                    .findByUserOrIsDefaultTrueOrderByName(user)
                    .stream()
                    .map(CategoryResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/default")
    public ResponseEntity<?> getDefaultCategories() {
        try {
            List<CategoryResponse> categories = categoryRepository
                    .findByIsDefaultTrueOrderByName()
                    .stream()
                    .map(CategoryResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/by-type/{type}")
    public ResponseEntity<?> getCategoriesByType(@PathVariable String type, Authentication authentication) {
        try {
            CategoryType categoryType = CategoryType.valueOf(type.toUpperCase());
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            List<CategoryResponse> categories = categoryRepository
                    .findByUserOrIsDefaultTrueOrderByName(user)
                    .stream()
                    .filter(category -> category.getCategoryType() == categoryType)
                    .map(CategoryResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(categories);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "유효하지 않은 카테고리 타입입니다");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/grouped")
    public ResponseEntity<?> getCategoriesGrouped(Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            List<CategoryResponse> allCategories = categoryRepository
                    .findByUserOrIsDefaultTrueOrderByName(user)
                    .stream()
                    .map(CategoryResponse::new)
                    .collect(Collectors.toList());

            Map<String, List<CategoryResponse>> groupedCategories = allCategories.stream()
                    .collect(Collectors.groupingBy(category -> category.getCategoryType().toString()));

            return ResponseEntity.ok(groupedCategories);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Map<String, String> categoryData, 
                                          Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            Category category = new Category();
            category.setName(categoryData.get("name"));
            category.setIcon(categoryData.get("icon"));
            category.setColor(categoryData.get("color"));
            category.setCategoryType(CategoryType.valueOf(categoryData.get("categoryType")));
            category.setIsDefault(false);
            category.setUser(user);
            category.setSortOrder(999); // 사용자 카테고리는 맨 뒤에

            Category savedCategory = categoryRepository.save(category);
            return ResponseEntity.ok(new CategoryResponse(savedCategory));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "카테고리 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id, 
                                          Authentication authentication) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다"));

            // 기본 카테고리는 삭제할 수 없음
            if (category.getIsDefault()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "기본 카테고리는 삭제할 수 없습니다");
                return ResponseEntity.badRequest().body(error);
            }

            // 본인의 카테고리인지 확인
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
            
            if (category.getUser() == null || !category.getUser().getId().equals(user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "삭제 권한이 없습니다");
                return ResponseEntity.badRequest().body(error);
            }

            categoryRepository.delete(category);
            
            Map<String, String> success = new HashMap<>();
            success.put("message", "카테고리가 삭제되었습니다");
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "카테고리 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}