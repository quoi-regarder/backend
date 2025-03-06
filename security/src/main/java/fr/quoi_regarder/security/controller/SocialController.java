package fr.quoi_regarder.security.controller;

import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.entity.user.User;
import fr.quoi_regarder.exception.exceptions.OAuthAuthenticationException;
import fr.quoi_regarder.mapper.user.UserMapper;
import fr.quoi_regarder.security.dto.LoginResponseDto;
import fr.quoi_regarder.security.service.JwtService;
import fr.quoi_regarder.security.service.SocialService;
import fr.quoi_regarder.security.service.TokenServiceSimple;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
public class SocialController {
    private final SocialService socialService;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final TokenServiceSimple tokenService;

    /**
     * Endpoint to get Google redirection URL
     */
    @GetMapping("/google")
    public ResponseEntity<ApiResponse<Map<String, String>>> googleRedirect(HttpSession session) {
        String redirectUrl = socialService.getGoogleRedirectUrl();

        // Store state in session for later validation
        if (redirectUrl.contains("state=")) {
            String state = redirectUrl.substring(redirectUrl.indexOf("state=") + 6);
            if (state.contains("&")) {
                state = state.substring(0, state.indexOf("&"));
            }
            session.setAttribute("oauth2_state", state);
        }

        Map<String, String> data = new HashMap<>();
        data.put("redirectUrl", redirectUrl);

        return ResponseEntity.ok(
                ApiResponse.success("Google authentication URL generated", data, HttpStatus.OK)
        );
    }

    /**
     * Callback endpoint to process Google response
     */
    @GetMapping("/google/callback")
    public ResponseEntity<ApiResponse<LoginResponseDto>> googleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            HttpServletRequest request) {

        // Verify state for CSRF protection
        HttpSession session = request.getSession(false);
        if (session != null && state != null) {
            String savedState = (String) session.getAttribute("oauth2_state");
            if (savedState == null || !savedState.equals(state)) {
                throw new OAuthAuthenticationException("Invalid authentication state", "google");
            }
            // Clean session after use
            session.removeAttribute("oauth2_state");
        }

        User user = socialService.handleGoogleCallback(code);

        String jwtToken = jwtService.generateToken(user);
        tokenService.saveToken(user.getEmail(), jwtToken);

        LoginResponseDto loginResponse = new LoginResponseDto();
        loginResponse.setToken(jwtToken);
        loginResponse.setUser(userMapper.toDto(user));

        return ResponseEntity.ok(
                ApiResponse.success("Google authentication successful", loginResponse, HttpStatus.OK)
        );
    }
}