package com.unble.budget.dto;

import com.unble.budget.entity.Category;
import com.unble.budget.entity.Category.CategoryType;

public class CategoryResponse {
    private Long id;
    private String name;
    private String color;
    private String icon;
    private Boolean isDefault;
    private CategoryType categoryType;

    public CategoryResponse() {}

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.color = category.getColor();
        this.icon = category.getIcon();
        this.isDefault = category.getIsDefault();
        this.categoryType = category.getCategoryType();
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

    public CategoryType getCategoryType() { return categoryType; }
    public void setCategoryType(CategoryType categoryType) { this.categoryType = categoryType; }
}