package fr.quoi_regarder.security.expression;

import fr.quoi_regarder.entity.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Class containing custom security expressions for users.
 * This class can be referenced in @PreAuthorize annotations using the @userSecurity syntax.
 */
@Component("userSecurity")
public class UserSecurityExpression {

    /**
     * Checks if the currently authenticated user has the specified ID.
     * Throws a ResponseStatusException with appropriate status and message if not authorized.
     *
     * @param userId the ID to check
     * @return true if the current user has this ID
     * @throws ResponseStatusException if the user doesn't have the specified ID
     */
    public boolean checkUserId(UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User not authenticated"
            );
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            UUID currentUserId = ((User) principal).getId();
            if (userId.equals(currentUserId)) {
                return true;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to access this resource"
        );
    }
}