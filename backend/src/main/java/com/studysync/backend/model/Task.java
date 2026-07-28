package com.studysync.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "tasks")
public class Task {
    @Id
    private String id;
    private String userId;
    private String title;
    private String courseName;
    private String description;
    private String dueDate;
    private String urgencyLevel; // LOW, MEDIUM, HIGH
    private String status;       // TODO, IN_PROGRESS, COMPLETED
    private List<SubTask> subTasks;

    public Task() {}

    public Task(String title, String courseName, String description, String dueDate, String urgencyLevel, String status) {
        this.title = title;
        this.courseName = courseName;
        this.description = description;
        this.dueDate = dueDate;
        this.urgencyLevel = urgencyLevel;
        this.status = status;
    }

    // Inner class for sub-tasks
    public static class SubTask {
        private String title;
        private boolean isCompleted;

        public SubTask() {}
        public SubTask(String title, boolean isCompleted) {
            this.title = title;
            this.isCompleted = isCompleted;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { isCompleted = completed; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<SubTask> getSubTasks() { return subTasks; }
    public void setSubTasks(List<SubTask> subTasks) { this.subTasks = subTasks; }
}