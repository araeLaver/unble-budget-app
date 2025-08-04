package com.unble.budget.controller;

import com.unble.budget.dto.TransactionRequest;
import com.unble.budget.entity.User;
import com.unble.budget.repository.UserRepository;
import com.unble.budget.service.DataImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/data-import")
public class DataImportController {

    @Autowired
    private DataImportService dataImportService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/csv")
    public ResponseEntity<Map<String, Object>> importFromCSV(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Map<String, Object> result = dataImportService.importFromCSV(user, file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/excel")
    public ResponseEntity<Map<String, Object>> importFromExcel(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Map<String, Object> result = dataImportService.importFromExcel(user, file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/json")
    public ResponseEntity<Map<String, Object>> importFromJSON(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Map<String, Object> result = dataImportService.importFromJSON(user, file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/template/csv")
    public ResponseEntity<String> getCSVTemplate() {
        String template = dataImportService.getCSVTemplate();
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=transaction_template.csv")
                .body(template);
    }

    @PostMapping("/preview")
    public ResponseEntity<Map<String, Object>> previewImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format,
            Authentication authentication) {
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Map<String, Object> preview = dataImportService.previewImport(user, file, format);
        return ResponseEntity.ok(preview);
    }
}