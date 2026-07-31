package com.studysync.backend.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

        // --- NEW TIMEZONE LOGIC STARTS HERE ---
        
        // Grab the exact strings from your Android app
        String dateString = task.getDueDate();
        // Provide a fallback of 09:00 just in case the time string is empty
        String timeString = (task.getDueTime() != null && !task.getDueTime().isEmpty()) ? task.getDueTime() : "09:00";

        // Stitch them together into a standard format
        LocalDateTime localDateTime = LocalDateTime.parse(dateString + "T" + timeString);

        // Lock it explicitly to the Kuala Lumpur timezone
        ZonedDateTime startZoned = localDateTime.atZone(ZoneId.of("Asia/Kuala_Lumpur"));
        // Default the task duration to 1 hour
        ZonedDateTime endZoned = startZoned.plusHours(1);

        // Convert to Google's required DateTime format
        // Convert to Google's required DateTime format using Date objects to bypass string formatting crashes
        DateTime startGoogleTime = new DateTime(java.util.Date.from(startZoned.toInstant()), java.util.TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        DateTime endGoogleTime = new DateTime(java.util.Date.from(endZoned.toInstant()), java.util.TimeZone.getTimeZone("Asia/Kuala_Lumpur"));

        // Attach them to the Event payload with the specific timezone string
        EventDateTime startEvent = new EventDateTime()
                .setDateTime(startGoogleTime)
                .setTimeZone("Asia/Kuala_Lumpur");

        EventDateTime endEvent = new EventDateTime()
                .setDateTime(endGoogleTime)
                .setTimeZone("Asia/Kuala_Lumpur");

        event.setStart(startEvent);
        event.setEnd(endEvent);
        
        // --- NEW TIMEZONE LOGIC ENDS HERE ---

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