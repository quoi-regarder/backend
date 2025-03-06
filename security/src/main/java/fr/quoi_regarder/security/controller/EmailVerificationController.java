package fr.quoi_regarder.security.controller;

import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.security.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/verify-email")
@RequiredArgsConstructor
public class EmailVerificationController {
    private final EmailVerificationService emailVerificationService;

    @GetMapping("/{token}")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@PathVariable String token) {
        emailVerificationService.verifyEmail(token);

        return ResponseEntity.ok(
                ApiResponse.success("Email successfully verified", HttpStatus.OK)
        );
    }
}