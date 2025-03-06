package fr.quoi_regarder.security.service;

import fr.quoi_regarder.commons.enums.ColorModeType;
import fr.quoi_regarder.commons.enums.LanguageIsoType;
import fr.quoi_regarder.commons.enums.RoleType;
import fr.quoi_regarder.entity.Role;
import fr.quoi_regarder.entity.user.Profile;
import fr.quoi_regarder.entity.user.User;
import fr.quoi_regarder.exception.exceptions.OAuthAuthenticationException;
import fr.quoi_regarder.repository.RoleRepository;
import fr.quoi_regarder.repository.user.ProfileRepository;
import fr.quoi_regarder.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SocialService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * Generates the redirection URL to Google authentication page with state for CSRF protection
     */
    public String getGoogleRedirectUrl() {
        ClientRegistration googleRegistration = clientRegistrationRepository.findByRegistrationId("google");
        if (googleRegistration == null) {
            throw new OAuthAuthenticationException("Google authentication is not configured", "google");
        }

        // Generate random state for CSRF protection
        String state = generateSecureState();

        // Build authorization URL using OAuth2AuthorizationRequest
        return OAuth2AuthorizationRequest.authorizationCode()
                .clientId(googleRegistration.getClientId())
                .authorizationUri(googleRegistration.getProviderDetails().getAuthorizationUri())
                .redirectUri(frontendUrl + "/auth/callback/google")
                .scopes(googleRegistration.getScopes())
                .state(state)
                .additionalParameters(params -> params.put("access_type", "offline"))
                .build()
                .getAuthorizationRequestUri();
    }

    /**
     * Process the authorization code received from Google
     * Uses a read-write transaction since it may create a new user
     */
    @Transactional
    public User handleGoogleCallback(String code) {
        try {
            ClientRegistration googleRegistration = clientRegistrationRepository.findByRegistrationId("google");
            if (googleRegistration == null) {
                throw new OAuthAuthenticationException("Google authentication is not configured", "google");
            }

            // Exchange code for access token
            String accessToken = getGoogleAccessToken(code, googleRegistration);

            // Get user information
            Map<String, Object> userInfo = getGoogleUserInfo(accessToken);

            // Check for required email field
            String email = (String) userInfo.get("email");
            if (email == null || email.trim().isEmpty()) {
                throw new OAuthAuthenticationException("Email information missing from Google account", "google");
            }

            // Check if user already exists, otherwise create
            return userRepository.findUserByEmail(email)
                    .orElseGet(() -> createUserFromGoogle(userInfo));
        } catch (RestClientException e) {
            // Wrap REST exceptions (will be handled by global handler)
            throw new OAuthAuthenticationException("Failed to authenticate with Google", "google", e);
        } catch (OAuthAuthenticationException e) {
            // Re-throw oauth exceptions
            throw e;
        } catch (Exception e) {
            // Wrap any other exceptions
            throw new OAuthAuthenticationException("Authentication failed", "google", e);
        }
    }

    /**
     * Exchange authorization code for access token
     */
    private String getGoogleAccessToken(String code, ClientRegistration registration) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("code", code);
        map.add("client_id", registration.getClientId());
        map.add("client_secret", registration.getClientSecret());
        map.add("redirect_uri", frontendUrl + "/auth/callback/google");
        map.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                registration.getProviderDetails().getTokenUri(),
                HttpMethod.POST,
                request,
                Map.class
        );

        Object accessToken = Objects.requireNonNull(response.getBody()).get("access_token");
        if (accessToken == null) {
            throw new OAuthAuthenticationException("Failed to obtain access token from Google", "google");
        }

        return (String) accessToken;
    }

    /**
     * Get user information with access token
     */
    private Map<String, Object> getGoogleUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> userInfo = response.getBody();
        if (userInfo == null) {
            throw new OAuthAuthenticationException("Failed to retrieve user information from Google", "google");
        }

        return userInfo;
    }

    /**
     * Create a new user from Google information
     * This is called within a transaction context from handleGoogleCallback
     */
    @Transactional
    protected User createUserFromGoogle(Map<String, Object> userInfo) {
        // Create user
        User user = new User();
        user.setEmail((String) userInfo.get("email"));
        user.setEmailVerified(true);

        // Set role
        Role role = roleRepository.findByName(RoleType.User)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);

        user = userRepository.save(user);

        // Create profile
        Profile profile = new Profile();
        profile.setUser(user);

        // Set username
        String username;
        if (userInfo.containsKey("name")) {
            username = (String) userInfo.get("name");
        } else if (userInfo.containsKey("given_name")) {
            username = (String) userInfo.get("given_name");
        } else {
            // Extract username from email
            String email = (String) userInfo.get("email");
            username = email.substring(0, email.indexOf('@'));
        }

        String usernameBase = username;

        // Ensure username is unique
        int counter = 1;
        while (profileRepository.existsByUsername(username)) {
            username = usernameBase + counter;
            counter++;
        }
        profile.setUsername(username);

        // Set profile information from Google data if available
        if (userInfo.containsKey("given_name")) {
            profile.setFirstName((String) userInfo.get("given_name"));
        }

        if (userInfo.containsKey("family_name")) {
            profile.setLastName((String) userInfo.get("family_name"));
        }

        if (userInfo.containsKey("picture")) {
            profile.setAvatarUrl((String) userInfo.get("picture"));
        }

        // Set default profile settings
        profile.setLanguage(LanguageIsoType.FR_FR.getCode());
        profile.setColorMode(ColorModeType.system);

        profileRepository.save(profile);

        return user;
    }

    /**
     * Generate secure state for CSRF protection
     */
    private String generateSecureState() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}