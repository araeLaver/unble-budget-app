package com.unble.budget.service;

import com.unble.budget.dto.UserSettingsRequest;
import com.unble.budget.dto.UserSettingsResponse;
import com.unble.budget.entity.User;
import com.unble.budget.entity.UserSettings;
import com.unble.budget.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserSettingsService {

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    public UserSettingsResponse getUserSettings(User user) {
        UserSettings settings = userSettingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));
        
        return new UserSettingsResponse(settings);
    }

    public UserSettingsResponse updateUserSettings(User user, UserSettingsRequest request) {
        UserSettings settings = userSettingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));

        settings.setCurrency(request.getCurrency());
        settings.setTheme(request.getTheme());
        settings.setNotificationEnabled(request.getNotificationEnabled());
        
        if (request.getLanguage() != null) {
            settings.setLanguage(request.getLanguage());
        }
        
        if (request.getDateFormat() != null) {
            settings.setDateFormat(request.getDateFormat());
        }
        
        if (request.getNumberFormat() != null) {
            settings.setNumberFormat(request.getNumberFormat());
        }

        settings = userSettingsRepository.save(settings);
        return new UserSettingsResponse(settings);
    }

    public UserSettingsResponse resetToDefault(User user) {
        UserSettings settings = userSettingsRepository.findByUser(user)
                .orElse(null);
        
        if (settings != null) {
            userSettingsRepository.delete(settings);
        }
        
        settings = createDefaultSettings(user);
        return new UserSettingsResponse(settings);
    }

    private UserSettings createDefaultSettings(User user) {
        UserSettings settings = new UserSettings();
        settings.setUser(user);
        settings.setCurrency("KRW");
        settings.setTheme("light");
        settings.setNotificationEnabled(true);
        settings.setLanguage("ko");
        settings.setDateFormat("YYYY-MM-DD");
        settings.setNumberFormat("#,##0");
        
        return userSettingsRepository.save(settings);
    }
}