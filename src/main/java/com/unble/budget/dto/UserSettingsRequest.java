package com.unble.budget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserSettingsRequest {
    
    @NotBlank(message = "통화는 필수입니다.")
    @Pattern(regexp = "^(KRW|USD|EUR|JPY|CNY)$", message = "지원하지 않는 통화입니다.")
    private String currency;
    
    @NotBlank(message = "테마는 필수입니다.")
    @Pattern(regexp = "^(light|dark|auto)$", message = "지원하지 않는 테마입니다.")
    private String theme;
    
    private Boolean notificationEnabled;
    
    private String language;
    
    private String dateFormat;
    
    private String numberFormat;

    public UserSettingsRequest() {}

    // Getters and Setters
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public Boolean getNotificationEnabled() { return notificationEnabled; }
    public void setNotificationEnabled(Boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }

    public String getNumberFormat() { return numberFormat; }
    public void setNumberFormat(String numberFormat) { this.numberFormat = numberFormat; }
}