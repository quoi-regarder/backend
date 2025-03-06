package fr.quoi_regarder.security.service;

import fr.quoi_regarder.commons.enums.TokenType;
import fr.quoi_regarder.entity.Token;
import fr.quoi_regarder.entity.user.User;
import fr.quoi_regarder.exception.exceptions.EntityNotExistsException;
import fr.quoi_regarder.exception.exceptions.InvalidTokenException;
import fr.quoi_regarder.repository.TokenRepository;
import fr.quoi_regarder.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void verifyEmail(String token) {
        Token verifyEmailToken = tokenRepository.findTokenByTypeAndExpiresAtAfterAndToken(TokenType.VERIFY_EMAIL, new Date(), token)
                .orElseThrow(() -> new InvalidTokenException(TokenType.VERIFY_EMAIL));

        User user = userRepository.findUserById(verifyEmailToken.getUser().getId())
                .orElseThrow(() -> new EntityNotExistsException(User.class, verifyEmailToken.getUser().getId().toString()));

        if (user.isEmailVerified()) {
            return;
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        tokenRepository.delete(verifyEmailToken);
    }
}
