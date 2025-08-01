package com.unble.budget.controller;

import com.unble.budget.dto.CategoryResponse;
import com.unble.budget.entity.Category.CategoryType;
import com.unble.budget.entity.User;
import com.unble.budget.repository.CategoryRepository;
import com.unble.budget.repository.UserRepository;
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
}