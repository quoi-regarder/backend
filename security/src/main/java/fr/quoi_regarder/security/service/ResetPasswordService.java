package fr.quoi_regarder.security.service;

import fr.quoi_regarder.commons.enums.TokenType;
import fr.quoi_regarder.entity.Token;
import fr.quoi_regarder.entity.user.Profile;
import fr.quoi_regarder.entity.user.User;
import fr.quoi_regarder.exception.exceptions.EntityNotExistsException;
import fr.quoi_regarder.exception.exceptions.InvalidTokenException;
import fr.quoi_regarder.repository.TokenRepository;
import fr.quoi_regarder.repository.user.ProfileRepository;
import fr.quoi_regarder.repository.user.UserRepository;
import fr.quoi_regarder.security.dto.SendResetPasswordDto;
import fr.quoi_regarder.security.dto.StoreResetPasswordDto;
import fr.quoi_regarder.service.mail.MailSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static fr.quoi_regarder.commons.utils.ExtractLocale.extractLocale;

@Service
@RequiredArgsConstructor
public class ResetPasswordService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final MessageSource messageSource;
    private final MailSenderService mailSenderService;

    @Value("${frontend.url}")
    private String frontendDomain;

    @Transactional
    public void sendResetPasswordEmail(SendResetPasswordDto sendResetPasswordDto) {
        User user = userRepository.findUserByEmail(sendResetPasswordDto.getEmail())
                .orElseThrow(() -> new EntityNotExistsException(User.class, sendResetPasswordDto.getEmail()));

        Profile profile = profileRepository.findProfileByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        Token resetPasswordToken = new Token();
        resetPasswordToken.setUser(user);
        resetPasswordToken.setToken(UUID.randomUUID().toString());
        // Expires in 1 hour
        resetPasswordToken.setExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000));
        resetPasswordToken.setType(TokenType.PASSWORD_RESET);

        tokenRepository.save(resetPasswordToken);

        // Prepare email data
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", profile.getUsername());
        variables.put("url", frontendDomain);
        variables.put("token", resetPasswordToken.getToken());
        variables.put("date", String.valueOf(Year.now().getValue()));

        String subject = messageSource.getMessage("email.password_reset.subject", null, extractLocale(profile.getLanguage()));

        // Send email asynchronously
        mailSenderService.sendMessageUsingThymeleafTemplateAsync(
                user.getEmail(),
                subject,
                variables,
                "password-reset",
                extractLocale(profile.getLanguage())
        );
    }

    @Transactional
    public User storeNewPassword(StoreResetPasswordDto storeResetPasswordDto) {
        Token token = tokenRepository.findTokenByTypeAndExpiresAtAfterAndToken(TokenType.PASSWORD_RESET, new Date(), storeResetPasswordDto.getToken())
                .orElseThrow(() -> new InvalidTokenException(TokenType.PASSWORD_RESET));

        User user = token.getUser();
        String userEmail = user.getEmail();
        String rawPassword = storeResetPasswordDto.getPassword();

        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userEmail,
                        rawPassword
                )
        );

        tokenRepository.delete(token);

        return user;
    }
}