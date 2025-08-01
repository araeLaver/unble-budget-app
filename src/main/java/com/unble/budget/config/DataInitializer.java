package com.unble.budget.config;

import com.unble.budget.entity.Category;
import com.unble.budget.entity.Category.CategoryType;
import com.unble.budget.entity.User;
import com.unble.budget.repository.CategoryRepository;
import com.unble.budget.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(CategoryRepository categoryRepository, 
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 관리자 계정 생성
        createAdminUser();
        
        // 기본 카테고리가 없을 때만 초기화
        if (categoryRepository.findByIsDefaultTrueOrderByName().isEmpty()) {
            createDefaultCategories();
        }
    }
    
    private void createAdminUser() {
        String adminEmail = "admin@unble.com";
        
        // 관리자 계정이 이미 존재하는지 확인
        if (!userRepository.findByEmail(adminEmail).isPresent()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setName("Administrator");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setIsActive(true);
            
            userRepository.save(admin);
            System.out.println("✅ 관리자 계정이 생성되었습니다: " + adminEmail);
        }
    }

    private void createDefaultCategories() {
        // 자산 관리 카테고리
        categoryRepository.save(new Category("현금", "#2ECC71", "💰", true, CategoryType.ASSET, 1));
        categoryRepository.save(new Category("은행예금", "#3498DB", "🏦", true, CategoryType.ASSET, 2));
        categoryRepository.save(new Category("적금", "#9B59B6", "💎", true, CategoryType.ASSET, 3));
        categoryRepository.save(new Category("투자", "#E67E22", "📈", true, CategoryType.ASSET, 4));
        categoryRepository.save(new Category("카드잔액", "#E74C3C", "💳", true, CategoryType.ASSET, 5));
        
        // 지출 카테고리
        categoryRepository.save(new Category("식비", "#FF6B6B", "🍽️", true, CategoryType.EXPENSE, 10));
        categoryRepository.save(new Category("교통비", "#4ECDC4", "🚗", true, CategoryType.EXPENSE, 11));
        categoryRepository.save(new Category("생활용품", "#45B7D1", "🛒", true, CategoryType.EXPENSE, 12));
        categoryRepository.save(new Category("의료비", "#96CEB4", "🏥", true, CategoryType.EXPENSE, 13));
        categoryRepository.save(new Category("문화/여가", "#FFEAA7", "🎬", true, CategoryType.EXPENSE, 14));
        categoryRepository.save(new Category("주거비", "#FF7675", "🏠", true, CategoryType.EXPENSE, 15));
        categoryRepository.save(new Category("교육비", "#6C5CE7", "📚", true, CategoryType.EXPENSE, 16));
        categoryRepository.save(new Category("의류/미용", "#FD79A8", "👕", true, CategoryType.EXPENSE, 17));
        categoryRepository.save(new Category("통신비", "#00B894", "📱", true, CategoryType.EXPENSE, 18));
        categoryRepository.save(new Category("공과금", "#FDCB6E", "⚡", true, CategoryType.EXPENSE, 19));
        categoryRepository.save(new Category("보험료", "#A29BFE", "🛡️", true, CategoryType.EXPENSE, 20));
        categoryRepository.save(new Category("세금", "#636E72", "🏛️", true, CategoryType.EXPENSE, 21));
        
        // 수입 카테고리
        categoryRepository.save(new Category("급여", "#27AE60", "💼", true, CategoryType.INCOME, 30));
        categoryRepository.save(new Category("용돈", "#E17055", "💵", true, CategoryType.INCOME, 31));
        categoryRepository.save(new Category("투자수익", "#00B894", "📊", true, CategoryType.INCOME, 32));
        categoryRepository.save(new Category("선물/보너스", "#FD79A8", "🎁", true, CategoryType.INCOME, 33));
        categoryRepository.save(new Category("부업소득", "#6C5CE7", "💰", true, CategoryType.INCOME, 34));
        categoryRepository.save(new Category("이자수익", "#00CEC9", "💹", true, CategoryType.INCOME, 35));
        
        // 기타
        categoryRepository.save(new Category("기타", "#DDA0DD", "📝", true, CategoryType.EXPENSE, 99));
        
        System.out.println("✅ 확장된 기본 카테고리가 초기화되었습니다 (정렬 순서 포함).");
    }
}