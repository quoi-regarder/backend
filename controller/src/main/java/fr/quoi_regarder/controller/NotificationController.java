package fr.quoi_regarder.controller;

import fr.quoi_regarder.entity.user.User;
import fr.quoi_regarder.exception.exceptions.sse.SseAuthenticationException;
import fr.quoi_regarder.service.sse.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller for real-time notifications using Server-Sent Events.
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final SseService sseService;

    /**
     * Subscribe to real-time notifications.
     * Authentication is handled by SseAuthenticationFilter.
     *
     * @param authentication Current user's authentication
     * @return SSE emitter for real-time updates
     */
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SseAuthenticationException("unknown");
        }

        User currentUser =
                (User) authentication.getPrincipal();

        return sseService.subscribe(currentUser.getId());
    }
}