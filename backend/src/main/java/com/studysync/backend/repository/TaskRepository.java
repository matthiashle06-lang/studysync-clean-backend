package com.studysync.backend.repository;

import com.studysync.backend.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByCourseName(String courseName);
}