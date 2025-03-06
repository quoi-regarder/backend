package fr.quoi_regarder.service.scheduled;

import fr.quoi_regarder.commons.enums.TokenType;
import fr.quoi_regarder.repository.TokenRepository;
import fr.quoi_regarder.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Log4j2
public class ScheduledTasks {
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredPasswordResetTokens() {
        tokenRepository.deleteByTypeAndExpiresAtBefore(TokenType.PASSWORD_RESET, new Date());
        log.info("[Scheduled Task] Deleted Expired Password Reset Tokens");
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteUserBasedOnVerifyEmailToken() {
        userRepository.deleteAllByTokenTypeAndExpiryDateBefore(TokenType.VERIFY_EMAIL.name(), new Date());
        tokenRepository.deleteByTypeAndExpiresAtBefore(TokenType.VERIFY_EMAIL, new Date());
        log.info("[Scheduled Task] Deleted Expired Verify Email Tokens");
    }
}

