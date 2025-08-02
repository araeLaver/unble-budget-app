package com.unble.budget.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 7)
    private String color = "#007AFF";

    @Column(length = 50)
    private String icon = "other";

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", length = 20)
    private CategoryType categoryType = CategoryType.EXPENSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public Category() {}

    public Category(String name, String color, String icon, Boolean isDefault) {
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.isDefault = isDefault;
    }

    public Category(String name, String color, String icon, Boolean isDefault, CategoryType categoryType) {
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.isDefault = isDefault;
        this.categoryType = categoryType;
    }

    public Category(String name, String color, String icon, Boolean isDefault, CategoryType categoryType, Integer sortOrder) {
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.isDefault = isDefault;
        this.categoryType = categoryType;
        this.sortOrder = sortOrder;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public CategoryType getCategoryType() { return categoryType; }
    public void setCategoryType(CategoryType categoryType) { this.categoryType = categoryType; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public enum CategoryType {
        INCOME,    // 수입
        EXPENSE,   // 지출
        ASSET      // 자산
    }
}