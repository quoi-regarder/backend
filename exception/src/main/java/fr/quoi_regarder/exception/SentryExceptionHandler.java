package fr.quoi_regarder.exception;

import io.sentry.Sentry;
import io.sentry.protocol.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler that logs uncaught exceptions to Sentry.
 * This handler has a lower precedence than GlobalExceptionHandler to ensure
 * it captures all exceptions not handled by other specific handlers.
 */
@Slf4j
@RestControllerAdvice
public class SentryExceptionHandler {

    /**
     * Captures all uncaught exceptions and sends them to Sentry with user context.
     *
     * @param ex The uncaught exception
     * @throws Exception The original exception is re-thrown for further handling
     */
    @ExceptionHandler(Exception.class)
    public void handleUncaughtException(Exception ex) throws Exception {
        enrichSentryContext();

        // Add additional context tags if needed
        Sentry.setTag("exceptionType", ex.getClass().getSimpleName());

        // Capture the exception with an appropriate level
        Sentry.captureException(ex);

        // Log the exception locally as well
        log.error("Uncaught exception captured and sent to Sentry", ex);

        // Re-throw the exception to allow other handlers or the default error mechanism to process it
        throw ex;
    }

    /**
     * Enriches the Sentry context with user information from Spring Security.
     */
    private void enrichSentryContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (isAuthenticatedUser(authentication)) {
                fr.quoi_regarder.entity.user.User currentUser =
                        (fr.quoi_regarder.entity.user.User) authentication.getPrincipal();

                User sentryUser = new User();
                sentryUser.setId(currentUser.getId().toString());
                sentryUser.setEmail(currentUser.getEmail());
                sentryUser.setUsername(currentUser.getUsername());

                // Add additional user context if available
                Sentry.setTag("userRole", authentication.getAuthorities().toString());

                Sentry.setUser(sentryUser);
            } else {
                setAnonymousUser();
            }
        } catch (Exception e) {
            setAnonymousUser();
            log.warn("Error configuring Sentry user context: {}", e.getMessage());
            Sentry.captureException(e);
        }
    }

    /**
     * Sets an anonymous user in the Sentry context.
     */
    private void setAnonymousUser() {
        User anonymousUser = new User();
        anonymousUser.setId("anonymous");
        Sentry.setUser(anonymousUser);
    }

    /**
     * Checks if the authentication represents a valid authenticated user.
     *
     * @param authentication The Spring Security authentication object
     * @return true if authentication represents a valid user
     */
    private boolean isAuthenticatedUser(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof fr.quoi_regarder.entity.user.User;
    }
}