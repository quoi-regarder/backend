package fr.quoi_regarder.controller.user;

import fr.quoi_regarder.commons.enums.ErrorStatus;
import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.exception.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/avatars")
@RequiredArgsConstructor
public class AvatarController {

    @Value("${app.storage.avatars-path:storage/avatars}")
    private String avatarsPath;

    /**
     * Downloads an avatar by its filename
     *
     * @param filename Name of the avatar file to retrieve
     * @return The requested avatar file or an error response if it doesn't exist
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<?> getAvatar(@PathVariable String filename) {
        if (filename == null || filename.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Avatar not found", ErrorStatus.RESSOURCE_NOT_FOUND, HttpStatus.NOT_FOUND));
        }

        try {
            Path filePath = Paths.get(avatarsPath, filename);
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Avatar file not found: " + filename);
            }

            // Detect the MIME type of the file
            String contentType = Files.probeContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(
                            "Avatar not found",
                            ErrorStatus.RESSOURCE_NOT_FOUND,
                            HttpStatus.NOT_FOUND));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(
                            "Error reading avatar file",
                            ErrorStatus.INTERNAL_SERVER_ERROR,
                            HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}