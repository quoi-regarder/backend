package fr.quoi_regarder.security.controller;

import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.dto.user.UserDto;
import fr.quoi_regarder.entity.user.User;
import fr.quoi_regarder.mapper.user.UserMapper;
import fr.quoi_regarder.security.dto.LoginDto;
import fr.quoi_regarder.security.dto.LoginResponseDto;
import fr.quoi_regarder.security.dto.RegisterDto;
import fr.quoi_regarder.security.service.AuthenticationService;
import fr.quoi_regarder.security.service.JwtService;
import fr.quoi_regarder.security.service.TokenServiceSimple;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;
    private final TokenServiceSimple tokenService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@RequestBody @Valid RegisterDto registerDto) {
        User user = authenticationService.signup(registerDto);
        UserDto userDto = userMapper.toDto(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", userDto, HttpStatus.CREATED));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody @Valid LoginDto loginDto) {
        User user = authenticationService.login(loginDto);

        String jwtToken = jwtService.generateToken(user);
        tokenService.saveToken(user.getEmail(), jwtToken);

        LoginResponseDto loginResponse = new LoginResponseDto();
        loginResponse.setToken(jwtToken);
        loginResponse.setUser(userMapper.toDto(user));

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", loginResponse, HttpStatus.OK)
        );
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            String username = jwtService.extractUsername(token);
            if (username != null) {
                tokenService.invalidateToken(username);
            }
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(
                ApiResponse.success("Logout successful", HttpStatus.OK)
        );
    }
}