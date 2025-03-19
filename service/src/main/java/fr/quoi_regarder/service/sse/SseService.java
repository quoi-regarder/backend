package fr.quoi_regarder.service.sse;

import fr.quoi_regarder.commons.enums.SseEventType;
import fr.quoi_regarder.exception.exceptions.sse.SseConnectionException;
import fr.quoi_regarder.exception.exceptions.sse.SseDeliveryException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service for managing Server-Sent Events connections and event delivery.
 */
@Service
public class SseService {
    // Map of user ID to active emitters
    private final Map<String, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    /**
     * Creates a new SSE connection for a user.
     *
     * @param userId the user's ID
     * @return the SseEmitter for this connection
     * @throws SseConnectionException if connection cannot be established
     */
    public SseEmitter subscribe(UUID userId) {
        String userIdString = userId.toString();
        SseEmitter emitter = new SseEmitter(0L); // No timeout

        try {
            // Add emitter to user's collection
            List<SseEmitter> emitters = userEmitters.computeIfAbsent(
                    userIdString, k -> new CopyOnWriteArrayList<>());
            emitters.add(emitter);

            // Configure completion callbacks
            emitter.onCompletion(() -> {
                removeEmitter(userIdString, emitter);
            });

            emitter.onTimeout(() -> {
                removeEmitter(userIdString, emitter);
            });

            emitter.onError(e -> {
                removeEmitter(userIdString, emitter);
            });

            // Send initial connection event
            emitter.send(SseEmitter.event()
                    .name("CONNECT")
                    .data("Connected to server events"));

            return emitter;

        } catch (IOException e) {
            removeEmitter(userIdString, emitter);
            throw new SseConnectionException(userIdString, e);
        }
    }

    /**
     * Removes an emitter from a user's collection.
     *
     * @param userId  the user's ID as string
     * @param emitter the emitter to remove
     */
    private void removeEmitter(String userId, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);

            // Clean up empty collections
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    /**
     * Sends an event to a specific user.
     *
     * @param userId    the user's ID
     * @param eventType the type of event
     * @param data      the event data
     * @return the number of successful deliveries
     */
    public int sendToUser(UUID userId, SseEventType eventType, Object data) {
        String userIdString = userId.toString();

        List<SseEmitter> emitters = userEmitters.get(userIdString);
        if (emitters == null || emitters.isEmpty()) {
            return 0;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();
        int successCount = 0;
        boolean hasDeliveryError = false;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventType.name())
                        .data(data, MediaType.APPLICATION_JSON));
                successCount++;
            } catch (Exception e) {
                // Check if this is a broken pipe or client disconnection exception
                if (e.getCause() instanceof IOException &&
                        (e.getCause().getMessage().contains("Broken pipe") ||
                                e.getMessage().contains("disconnected"))) {
                    // This is normal when clients disconnect, just remove the emitter
                } else {
                    // For other exceptions, mark as a delivery error
                    hasDeliveryError = true;
                }
                deadEmitters.add(emitter);
            }
        }

        // Clean up failed emitters
        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }

        // Only throw exception for real errors, not client disconnections
        if (hasDeliveryError && successCount == 0) {
            throw new SseDeliveryException(userIdString, eventType.name(), data);
        }

        return successCount;
    }

    /**
     * Checks if a user has active SSE connections.
     *
     * @param userId the user's ID
     * @return true if the user has active connections
     */
    public boolean hasActiveConnections(UUID userId) {
        String userIdString = userId.toString();
        List<SseEmitter> emitters = userEmitters.get(userIdString);
        return emitters != null && !emitters.isEmpty();
    }

    /**
     * Gets the count of active connections for a specific user.
     *
     * @param userId the user's ID
     * @return the number of active connections
     */
    public int getUserConnectionCount(UUID userId) {
        String userIdString = userId.toString();
        List<SseEmitter> emitters = userEmitters.get(userIdString);
        return emitters != null ? emitters.size() : 0;
    }

    /**
     * Gets the total number of active SSE connections across all users.
     *
     * @return the total connection count
     */
    public int getTotalConnectionCount() {
        return userEmitters.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Gets the number of users with active connections.
     *
     * @return the number of users with active connections
     */
    public int getActiveUserCount() {
        return userEmitters.size();
    }
}