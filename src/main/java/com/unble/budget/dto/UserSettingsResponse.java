package com.unble.budget.dto;

import com.unble.budget.entity.UserSettings;

public class UserSettingsResponse {
    private Long id;
    private String currency;
    private String theme;
    private Boolean notificationEnabled;
    private String language;
    private String dateFormat;
    private String numberFormat;

    public UserSettingsResponse() {}

    public UserSettingsResponse(UserSettings userSettings) {
        this.id = userSettings.getId();
        this.currency = userSettings.getCurrency();
        this.theme = userSettings.getTheme();
        this.notificationEnabled = userSettings.getNotificationEnabled();
        this.language = userSettings.getLanguage();
        this.dateFormat = userSettings.getDateFormat();
        this.numberFormat = userSettings.getNumberFormat();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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