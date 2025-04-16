package fr.quoi_regarder.service.user;

import fr.quoi_regarder.commons.enums.LanguageIsoType;
import fr.quoi_regarder.dto.user.ProfileDto;
import fr.quoi_regarder.dto.user.UpdateColorModeDto;
import fr.quoi_regarder.dto.user.UpdateLanguageDto;
import fr.quoi_regarder.dto.user.UpdateProfileDto;
import fr.quoi_regarder.entity.user.Profile;
import fr.quoi_regarder.event.ProfileUpdatedEvent;
import fr.quoi_regarder.exception.exceptions.*;
import fr.quoi_regarder.mapper.user.ProfileMapper;
import fr.quoi_regarder.repository.user.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private static final List<String> ALLOWED_AVATAR_EXTENSIONS = List.of("webp");
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final ApplicationEventPublisher eventPublisher;
    @Value("${app.storage.avatars-path:storage/avatars}")
    private String avatarsPath;

    public ProfileDto getProfile(UUID userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotExistsException(Profile.class, userId.toString()));

        return profileMapper.toDto(profile);
    }

    public String getAvatarUrl(UUID userId) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotExistsException(Profile.class, userId.toString()));

        return profile.getAvatarUrl();
    }

    @Transactional
    public ProfileDto updateProfile(UUID userId, UpdateProfileDto profileDto) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotExistsException(Profile.class, userId.toString()));

        if (profileRepository.existsByUsernameAndUserIdNot(profileDto.getUsername(), userId)) {
            throw new UserAlreadyExists("username", profile.getUsername());
        }

        profileMapper.partialUpdate(profile, profileDto);
        ProfileDto updatedProfileDto = profileMapper.toDto(profileRepository.save(profile));

        eventPublisher.publishEvent(new ProfileUpdatedEvent(this, userId, updatedProfileDto));

        return updatedProfileDto;
    }

    @Transactional
    public ProfileDto updateLanguage(UUID userId, UpdateLanguageDto languageDto) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotExistsException(Profile.class, userId.toString()));

        try {
            LanguageIsoType.findByCode(languageDto.getLanguage());
        } catch (IllegalArgumentException e) {
            throw new InvalidLanguageException(languageDto.getLanguage());
        }

        profile.setLanguage(languageDto.getLanguage());

        return profileMapper.toDto(profileRepository.save(profile));
    }

    @Transactional
    public ProfileDto updateColorMode(UUID userId, UpdateColorModeDto colorModeDto) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotExistsException(Profile.class, userId.toString()));

        profile.setColorMode(colorModeDto.getColorMode());

        return profileMapper.toDto(profileRepository.save(profile));
    }

    @Transactional
    public ProfileDto updateAvatar(UUID userId, MultipartFile avatarFile) throws IOException {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotExistsException(Profile.class, userId.toString()));

        // Delete previous avatar if exists
        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().startsWith("http")) {
            try {
                deleteAvatarFile(profile.getAvatarUrl());
            } catch (Exception e) {
                // Continue with the upload even if delete fails
            }
        }

        // Handle case where no new avatar is provided (just removing)
        if (avatarFile == null || avatarFile.isEmpty()) {
            profile.setAvatarUrl(null);
            return profileMapper.toDto(profileRepository.save(profile));
        }

        // Validate file
        validateAvatarFile(avatarFile);

        // Save new avatar file
        String fileName = generateAvatarFileName(userId, getFileExtension(avatarFile.getOriginalFilename()));
        saveAvatarFile(avatarFile, fileName);

        // Update profile
        profile.setAvatarUrl(fileName);


        ProfileDto profileDto = profileMapper.toDto(profileRepository.save(profile));

        eventPublisher.publishEvent(new ProfileUpdatedEvent(this, userId, profileDto));

        return profileDto;
    }

    @Transactional
    public ProfileDto deleteAvatar(UUID userId) throws IOException {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotExistsException(Profile.class, userId.toString()));

        // Delete avatar file if exists
        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().startsWith("http")) {
            try {
                deleteAvatarFile(profile.getAvatarUrl());
            } catch (Exception e) {
                // Continue with the update even if delete fails
            }
        }

        // Update profile
        profile.setAvatarUrl(null);

        ProfileDto profileDto = profileMapper.toDto(profileRepository.save(profile));

        eventPublisher.publishEvent(new ProfileUpdatedEvent(this, userId, profileDto));

        return profileDto;
    }

    @Transactional
    public ProfileDto updateOnboarding(UUID userId, boolean onboarding) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotExistsException(Profile.class, userId.toString()));

        profile.setOnboarding(onboarding);

        eventPublisher.publishEvent(new ProfileUpdatedEvent(this, userId, profileMapper.toDto(profile)));

        return profileMapper.toDto(profileRepository.save(profile));
    }

    private void validateAvatarFile(MultipartFile file) {
        // Check file size
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new InvalidFileException(
                    "Avatar file size exceeds maximum allowed size",
                    "file_too_large",
                    "Maximum size is 5MB, received " + (file.getSize() / 1024 / 1024) + "MB"
            );
        }

        // Check file extension
        String extension = getFileExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_AVATAR_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidFileException(
                    "Invalid file format",
                    "invalid_format",
                    "Only WebP images are allowed, received: " + extension
            );
        }

        // Check for WebP content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("image/webp")) {
            throw new InvalidFileException(
                    "Invalid content type",
                    "invalid_content_type",
                    "Only WebP images are allowed, received content type: " + contentType
            );
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    private String generateAvatarFileName(UUID userId, String extension) {
        return userId.toString() + "_" + System.currentTimeMillis() + "." + extension;
    }

    private void saveAvatarFile(MultipartFile file, String fileName) throws IOException {
        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(avatarsPath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file
        Path destination = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private void deleteAvatarFile(String fileName) throws IOException {
        Path filePath = Paths.get(avatarsPath, fileName);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        } else {
            throw new ResourceNotFoundException("Avatar file not found", fileName);
        }
    }
}