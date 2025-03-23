package fr.quoi_regarder.controller.user;

import fr.quoi_regarder.commons.enums.ErrorStatus;
import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.dto.user.ProfileDto;
import fr.quoi_regarder.dto.user.UpdateColorModeDto;
import fr.quoi_regarder.dto.user.UpdateLanguageDto;
import fr.quoi_regarder.dto.user.UpdateProfileDto;
import fr.quoi_regarder.service.user.ProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@PreAuthorize("@userSecurity.checkUserId(#userId)")
@SecurityRequirement(name = "Bearer Authentication")
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<ProfileDto>> getUserProfile(@PathVariable UUID userId) {
        ProfileDto profile = profileService.getProfile(userId);
        return ResponseEntity.ok(
                ApiResponse.success("User profile retrieved successfully", profile, HttpStatus.OK)
        );
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<ProfileDto>> updateUserProfile(@PathVariable UUID userId, @RequestBody UpdateProfileDto profileDto) {
        ProfileDto updatedProfile = profileService.updateProfile(userId, profileDto);
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", updatedProfile, HttpStatus.OK)
        );
    }

    @GetMapping("/{userId}/avatar")
    public ResponseEntity<ApiResponse<String>> getUserAvatar(@PathVariable UUID userId) {
        String avatarUrl = profileService.getAvatarUrl(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Avatar URL retrieved successfully", avatarUrl, HttpStatus.OK)
        );
    }

    @PutMapping(value = "/{userId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProfileDto>> uploadAvatar(
            @PathVariable UUID userId,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar) {

        try {
            ProfileDto updatedProfile = profileService.updateAvatar(userId, avatar);
            return ResponseEntity.ok(
                    ApiResponse.success("Avatar uploaded successfully", updatedProfile, HttpStatus.OK)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            e.getMessage(),
                            ErrorStatus.BAD_REQUEST,
                            HttpStatus.BAD_REQUEST
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            "Could not upload avatar: " + e.getMessage(),
                            ErrorStatus.INTERNAL_SERVER_ERROR,
                            HttpStatus.INTERNAL_SERVER_ERROR
                    ));
        }
    }

    @DeleteMapping("/{userId}/avatar")
    public ResponseEntity<ApiResponse<ProfileDto>> deleteAvatar(@PathVariable UUID userId) {
        try {
            ProfileDto updatedProfile = profileService.deleteAvatar(userId);
            return ResponseEntity.ok(
                    ApiResponse.success("Avatar deleted successfully", updatedProfile, HttpStatus.OK)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            "Could not delete avatar: " + e.getMessage(),
                            ErrorStatus.INTERNAL_SERVER_ERROR,
                            HttpStatus.INTERNAL_SERVER_ERROR
                    ));
        }
    }

    @PutMapping("/{userId}/language")
    public ResponseEntity<ApiResponse<ProfileDto>> updateUserLanguage(@PathVariable UUID userId, @RequestBody UpdateLanguageDto languageDto) {
        ProfileDto updatedProfile = profileService.updateLanguage(userId, languageDto);
        return ResponseEntity.ok(
                ApiResponse.success("Language preference updated successfully", updatedProfile, HttpStatus.OK)
        );
    }

    @PutMapping("/{userId}/color-mode")
    public ResponseEntity<ApiResponse<ProfileDto>> updateUserColorMode(@PathVariable UUID userId, @RequestBody UpdateColorModeDto colorModeDto) {
        ProfileDto updatedProfile = profileService.updateColorMode(userId, colorModeDto);
        return ResponseEntity.ok(
                ApiResponse.success("Color mode preference updated successfully", updatedProfile, HttpStatus.OK)
        );
    }
}