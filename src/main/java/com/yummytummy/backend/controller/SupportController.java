package com.yummytummy.backend.controller;

import com.yummytummy.backend.entity.SupportMessage;
import com.yummytummy.backend.repository.SupportMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final SupportMessageRepository supportMessageRepository;

    public SupportController(SupportMessageRepository supportMessageRepository) {
        this.supportMessageRepository = supportMessageRepository;
    }

    @PostMapping("/message")
    public ResponseEntity<?> submitMessage(@RequestBody SupportMessage message) {
        try {
            if (message.getName() == null || message.getEmail() == null || message.getMessage() == null) {
                return ResponseEntity.badRequest().body("Please fill in all required fields.");
            }
            SupportMessage saved = supportMessageRepository.save(message);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while saving your message.");
        }
    }

    // Admin endpoints
    @GetMapping("/messages")
    public ResponseEntity<List<SupportMessage>> getAllMessages() {
        return ResponseEntity.ok(supportMessageRepository.findAll());
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Integer id) {
        supportMessageRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
