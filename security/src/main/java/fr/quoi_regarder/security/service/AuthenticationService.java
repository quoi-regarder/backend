package fr.quoi_regarder.security.service;

import fr.quoi_regarder.commons.enums.LanguageIsoType;
import fr.quoi_regarder.commons.enums.RoleType;
import fr.quoi_regarder.commons.enums.TokenType;
import fr.quoi_regarder.entity.Role;
import fr.quoi_regarder.entity.Token;
import fr.quoi_regarder.entity.user.Profile;
import fr.quoi_regarder.entity.user.User;
import fr.quoi_regarder.exception.exceptions.BadCredentialsException;
import fr.quoi_regarder.exception.exceptions.EmailNotVerifiedException;
import fr.quoi_regarder.exception.exceptions.InvalidLanguageException;
import fr.quoi_regarder.exception.exceptions.UserAlreadyExists;
import fr.quoi_regarder.repository.RoleRepository;
import fr.quoi_regarder.repository.TokenRepository;
import fr.quoi_regarder.repository.user.ProfileRepository;
import fr.quoi_regarder.repository.user.UserRepository;
import fr.quoi_regarder.security.dto.LoginDto;
import fr.quoi_regarder.security.dto.RegisterDto;
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
public class AuthenticationService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final TokenRepository tokenRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final MessageSource messageSource;
    private final MailSenderService mailSenderService;
    private final LoginAttemptService loginAttemptService;

    @Value("${frontend.url}")
    private String frontendDomain;

    @Transactional
    public User signup(RegisterDto registerDto) {
        // Check if user already exists
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new UserAlreadyExists("email", registerDto.getEmail());
        }
        if (profileRepository.existsByUsername(registerDto.getUsername())) {
            throw new UserAlreadyExists("username", registerDto.getUsername());
        }

        try {
            LanguageIsoType.findByCode(registerDto.getLanguage());
        } catch (IllegalArgumentException e) {
            throw new InvalidLanguageException(registerDto.getLanguage());
        }

        // Create user
        User user = new User();
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Role role = roleRepository.findByName(RoleType.User)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRole(role);
        user = userRepository.save(user);

        // Create profile
        Profile profile = new Profile();

        profile.setUser(user);
        profile.setUsername(registerDto.getUsername());
        profile.setFirstName(registerDto.getFirstName());
        profile.setLastName(registerDto.getLastName());
        profile.setLanguage(registerDto.getLanguage());
        profile.setColorMode(registerDto.getColorMode());

        profileRepository.save(profile);

        // Create token
        Token token = new Token();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        // Expires in 24 hours
        token.setExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
        token.setType(TokenType.VERIFY_EMAIL);

        tokenRepository.save(token);

        // Email preparation
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", profile.getUsername());
        variables.put("url", frontendDomain);
        variables.put("token", token.getToken());
        variables.put("date", String.valueOf(Year.now().getValue()));

        String subject = messageSource.getMessage("email.verification.subject", null, extractLocale(registerDto.getLanguage()));

        // Send email asynchronously
        mailSenderService.sendMessageUsingThymeleafTemplateAsync(
                user.getEmail(),
                subject,
                variables,
                "email-verification",
                extractLocale(registerDto.getLanguage())
        );

        return user;
    }

    public User login(LoginDto loginDto) {
        if (loginAttemptService.isBlocked()) {
            int resetDelay = loginAttemptService.getResetDelay();
            throw new BadCredentialsException(0L, (long) resetDelay);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmail(),
                            loginDto.getPassword()
                    )
            );
        } catch (Exception e) {
            loginAttemptService.loginFailed();

            int attemptsLeft = loginAttemptService.getRemainingAttempts();

            if (attemptsLeft != 0) {
                throw new BadCredentialsException((long) attemptsLeft, null);
            }

            int resetDelay = loginAttemptService.getResetDelay();

            throw new BadCredentialsException((long) attemptsLeft, (long) resetDelay);
        }

        loginAttemptService.loginSucceeded();

        User user = userRepository.findUserByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email not verified");
        }

        return user;
    }
}