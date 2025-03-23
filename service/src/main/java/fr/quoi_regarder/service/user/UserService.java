package fr.quoi_regarder.service.user;

import fr.quoi_regarder.entity.user.Profile;
import fr.quoi_regarder.entity.user.User;
import fr.quoi_regarder.exception.exceptions.EntityNotExistsException;
import fr.quoi_regarder.repository.user.ProfileRepository;
import fr.quoi_regarder.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Transactional
    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Gets the current authenticated user's preferred language
     *
     * @return Language code
     */
    public String getCurrentUserLanguage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }

        User user = (User) auth.getPrincipal();

        return profileRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotExistsException(Profile.class, user.getId().toString()))
                .getLanguage();
    }
}