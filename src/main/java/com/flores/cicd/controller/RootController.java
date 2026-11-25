package com.flores.cicd.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "To-Do List API");
        info.put("status", "running");
        info.put("timestamp", LocalDateTime.now());
        info.put("endpoints", Map.of(
                "health", "/api/tasks/health",
                "list_tasks", "GET /api/tasks",
                "create_task", "POST /api/tasks",
                "get_task", "GET /api/tasks/{id}",
                "update_task", "PUT /api/tasks/{id}",
                "delete_task", "DELETE /api/tasks/{id}"
        ));
        return ResponseEntity.ok(info);
    }
}
