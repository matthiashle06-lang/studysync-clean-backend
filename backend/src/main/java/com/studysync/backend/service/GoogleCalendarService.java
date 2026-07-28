package com.studysync.backend.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.studysync.backend.model.Task;
import com.studysync.backend.model.UserToken;
import com.studysync.backend.repository.TaskRepository;
import com.studysync.backend.repository.UserTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class GoogleCalendarService {

    @Autowired
    private UserTokenRepository tokenRepository;

    @Autowired
    private TaskRepository taskRepository;

    private Calendar getCalendarService(String accessToken) throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("StudySync")
                .build();
    }

    // 1. Push a StudySync Task to Google Calendar
    public String exportTaskToGoogleCalendar(String userId, Task task) throws Exception {
        UserToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not authenticated with Google"));

        Calendar service = getCalendarService(token.getAccessToken());

        Event event = new Event()
                .setSummary("[" + task.getCourseName() + "] " + task.getTitle())
                .setDescription("Urgency: " + task.getUrgencyLevel() + "\n" + task.getDescription());

        // Format due date to RFC3339 String
        EventDateTime start = new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(task.getDueDate() + "T09:00:00Z"));
        EventDateTime end = new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(task.getDueDate() + "T10:00:00Z"));
        event.setStart(start);
        event.setEnd(end);

        Event createdEvent = service.events().insert("primary", event).execute();
        return createdEvent.getId(); // Returns Google Event ID
    }

    // 2. Incremental Sync using Google Sync Tokens (Mitigates Rate Limits & Cold Starts)
    public List<Event> syncIncrementalEvents(String userId) throws Exception {
        UserToken token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User token missing"));

        Calendar service = getCalendarService(token.getAccessToken());
        Calendar.Events.List request = service.events().list("primary");

        // Use Sync Token if present, avoiding full re-fetch
        if (token.getSyncToken() != null && !token.getSyncToken().isEmpty()) {
            request.setSyncToken(token.getSyncToken());
        }

        Events events = request.execute();
        
        // Save new Sync Token for the next pull request
        token.setSyncToken(events.getNextSyncToken());
        tokenRepository.save(token);

        return events.getItems();
    }
}