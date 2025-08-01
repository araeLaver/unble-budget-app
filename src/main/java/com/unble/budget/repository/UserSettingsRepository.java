package com.unble.budget.repository;

import com.unble.budget.entity.User;
import com.unble.budget.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    Optional<UserSettings> findByUser(User user);
    boolean existsByUser(User user);
    void deleteByUser(User user);
}