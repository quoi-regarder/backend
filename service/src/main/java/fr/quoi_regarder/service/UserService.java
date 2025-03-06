package fr.quoi_regarder.service;

import fr.quoi_regarder.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }
}
