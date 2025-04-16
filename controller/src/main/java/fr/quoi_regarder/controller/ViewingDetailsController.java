package fr.quoi_regarder.controller;

import fr.quoi_regarder.dto.ViewingDetailsDto;
import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.service.ViewingDetailsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/viewing-details/{userId}")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("@userSecurity.checkUserId(#userId)")
public class ViewingDetailsController {
    private final ViewingDetailsService viewingDetailsService;

    @GetMapping("/{contentId}")
    public ResponseEntity<ApiResponse<ViewingDetailsDto>> getContentDetails(
            @PathVariable UUID userId,
            @PathVariable Long contentId
    ) {
        ViewingDetailsDto viewingDetailsDto = viewingDetailsService.getViewingDetails(userId, contentId);

        if (viewingDetailsDto == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success("No viewing details found", null, HttpStatus.NO_CONTENT));
        }

        if (viewingDetailsDto.getContextId() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Content not found", null, HttpStatus.NOT_FOUND));
        }

        return ResponseEntity.ok(ApiResponse.success("Content details found", viewingDetailsDto, HttpStatus.OK));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ViewingDetailsDto>> addContentDetails(
            @PathVariable UUID userId,
            @RequestBody ViewingDetailsDto viewingDetailsDto
    ) {
        ViewingDetailsDto createdViewingDetails = viewingDetailsService.addViewingDetails(userId, viewingDetailsDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Content details created", createdViewingDetails, HttpStatus.CREATED));
    }

    @PutMapping("/{contentId}")
    public ResponseEntity<ApiResponse<ViewingDetailsDto>> updateContentDetails(
            @PathVariable UUID userId,
            @PathVariable Long contentId,
            @RequestBody ViewingDetailsDto viewingDetailsDto
    ) {
        ViewingDetailsDto updatedViewingDetails = viewingDetailsService.updateViewingDetails(userId, contentId, viewingDetailsDto);
        return ResponseEntity.ok(ApiResponse.success("Content details updated", updatedViewingDetails, HttpStatus.OK));
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<ApiResponse<Void>> deleteContentDetails(
            @PathVariable UUID userId,
            @PathVariable Long contentId
    ) {
        viewingDetailsService.deleteViewingDetails(userId, contentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("Content details deleted", null, HttpStatus.NO_CONTENT));
    }
}
