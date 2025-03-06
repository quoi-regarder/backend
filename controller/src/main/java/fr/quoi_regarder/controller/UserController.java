package fr.quoi_regarder.controller;

import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{userId}")
    @PreAuthorize("@userSecurity.checkUserId(#userId)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User successfully deleted", HttpStatus.OK)
        );
    }
}