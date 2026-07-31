package com.studysync.backend.controller;

import com.studysync.backend.model.Task;
import com.studysync.backend.repository.TaskRepository;
import com.studysync.backend.service.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@CrossOrigin(origins = "*") 
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    // 1. Inject the Calendar Service here
    @Autowired
    private GoogleCalendarService calendarService;

    // --> NEW: Fetch tasks only for a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getTasksByUser(@PathVariable String userId) {
        List<Task> userTasks = taskRepository.findByUserId(userId);
        return ResponseEntity.ok(userTasks);
    }

    // Keep this for admin/testing purposes
    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // 2. Grab the userId, attach it to the task, save to DB, and push to Google Calendar
    @PostMapping
    public ResponseEntity<?> createTask(@RequestParam String userId, @RequestBody Task task) {
        try {
            // Guarantee the user ID is explicitly attached before saving to MongoDB
            task.setUserId(userId);
            Task savedTask = taskRepository.save(task);
            
            // Push to Google Calendar
            calendarService.exportTaskToGoogleCalendar(userId, savedTask);
            
            return ResponseEntity.ok(savedTask);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Task saved to DB, but Calendar sync failed: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable String id, @RequestBody Task updatedTask) {
        return taskRepository.findById(id).map(task -> {
            task.setTitle(updatedTask.getTitle());
            task.setCourseName(updatedTask.getCourseName());
            task.setDescription(updatedTask.getDescription());
            task.setDueDate(updatedTask.getDueDate());
            task.setUrgencyLevel(updatedTask.getUrgencyLevel());
            task.setStatus(updatedTask.getStatus());
            task.setSubTasks(updatedTask.getSubTasks());
            return ResponseEntity.ok(taskRepository.save(task));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}