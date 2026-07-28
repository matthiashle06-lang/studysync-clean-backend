package com.studysync.backend.controller;

import com.studysync.backend.service.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sync")
@CrossOrigin(origins = "*")
public class CalendarController {

    @Autowired
    private GoogleCalendarService calendarService;

    @PostMapping("/google-calendar")
    public ResponseEntity<String> triggerSync(@RequestParam String userId) {
        try {
            calendarService.syncIncrementalEvents(userId);
            return ResponseEntity.ok("Google Calendar synchronized successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Sync failed: " + e.getMessage());
        }
    }
}