package fr.quoi_regarder.security.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    // Delay before resetting the attempts counter (30 minutes)
    private static final int RESET_DELAY_MILLIS = 30 * 60 * 1000;

    private final HttpServletRequest request;

    // Cache to store login attempts by IP address
    private final Cache<String, Integer> attemptsCache = Caffeine.newBuilder()
            .expireAfterWrite(RESET_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .maximumSize(10_000)
            .build();

    // Cache to store the timestamp of the last failed attempt
    private final Cache<String, Long> blockTimeCache = Caffeine.newBuilder()
            .expireAfterWrite(RESET_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .maximumSize(10_000)
            .build();

    /**
     * Increments the failed login attempts counter for the current IP
     */
    public void loginFailed() {
        String ipAddress = getClientIP();
        Integer attempts = attemptsCache.getIfPresent(ipAddress);
        int newAttempts = (attempts == null) ? 1 : attempts + 1;
        attemptsCache.put(ipAddress, newAttempts);

        // Store the block time if this attempt exceeds the threshold
        if (newAttempts >= MAX_ATTEMPTS) {
            blockTimeCache.put(ipAddress, System.currentTimeMillis());
        }
    }

    /**
     * Resets the attempts counter for the current IP
     */
    public void loginSucceeded() {
        String ipAddress = getClientIP();
        attemptsCache.invalidate(ipAddress);
        blockTimeCache.invalidate(ipAddress);
    }

    /**
     * Checks if the current IP is blocked
     *
     * @return true if the IP is blocked
     */
    public boolean isBlocked() {
        String ipAddress = getClientIP();
        Integer attempts = attemptsCache.getIfPresent(ipAddress);
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }

    /**
     * Gets the number of remaining attempts before blocking
     *
     * @return the number of remaining attempts
     */
    public int getRemainingAttempts() {
        String ipAddress = getClientIP();
        Integer attempts = attemptsCache.getIfPresent(ipAddress);
        return Math.max(MAX_ATTEMPTS - (attempts == null ? 0 : attempts), 0);
    }

    /**
     * Gets the remaining time in minutes before the block is reset for the current IP
     *
     * @return the remaining time in minutes, or 0 if not blocked
     */
    public int getResetDelay() {
        String ipAddress = getClientIP();
        Integer attempts = attemptsCache.getIfPresent(ipAddress);
        if (attempts == null || attempts < MAX_ATTEMPTS) {
            return 0;
        }

        Long blockTime = blockTimeCache.getIfPresent(ipAddress);
        if (blockTime == null) {
            return 0;
        }

        long elapsedMillis = System.currentTimeMillis() - blockTime;
        long remainingMillis = Math.max(0, RESET_DELAY_MILLIS - elapsedMillis);

        // Convert to minutes and round up
        return (int) Math.ceil(remainingMillis / (60 * 1000.0));
    }

    /**
     * Gets the client's IP address
     *
     * @return the IP address
     */
    private String getClientIP() {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}