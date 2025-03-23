package fr.quoi_regarder.controller.serie;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.dto.movie.UpdateStatusRequest;
import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.dto.serie.SerieDto;
import fr.quoi_regarder.dto.serie.SerieWatchlistDto;
import fr.quoi_regarder.service.serie.SerieWatchlistEventService;
import fr.quoi_regarder.service.serie.SerieWatchlistService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/serie-watchlist/{userId}/serie")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("@userSecurity.checkUserId(#userId)")
public class SerieWatchlistController {
    private final SerieWatchlistEventService serieWatchlistEventService;
    private final SerieWatchlistService serieWatchlistService;

    /**
     * Get all series IDs in a user's watchlist grouped by status
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<Long>>>> getWatchlist(
            @PathVariable UUID userId) {
        Map<String, List<Long>> result = serieWatchlistEventService.findWatchlistByUserId(userId);

        return ResponseEntity.ok(ApiResponse.success("Series found", result, HttpStatus.OK));
    }

    /**
     * Get detailed series information from watchlist with pagination
     */
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<Page<SerieDto>>> getSerieDetails(
            @PathVariable UUID userId,
            @RequestParam WatchStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Page<SerieDto> series = serieWatchlistService.findSeriesByUserIdAndStatus(userId, status, page, limit);
        return ResponseEntity.ok(ApiResponse.success("Series found", series, HttpStatus.OK));
    }

    /**
     * Get total runtime for user's watched series
     */
    @GetMapping("/runtime")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTotalRuntime(
            @PathVariable UUID userId) {
        Map<String, Long> result = serieWatchlistService.calculateTotalRuntimeForUser(userId);

        return ResponseEntity.ok(ApiResponse.success("Total runtime found", result, HttpStatus.OK));
    }

    /**
     * Add a serie to watchlist
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addSerie(
            @PathVariable UUID userId,
            @Valid @RequestBody SerieWatchlistDto serieWatchlistDTO) {
        serieWatchlistService.addToWatchlist(userId, serieWatchlistDTO.getTmdbId(), serieWatchlistDTO);
        return ResponseEntity.ok(ApiResponse.success("Serie added to watchlist", null, HttpStatus.OK));
    }

    /**
     * Update serie watch status
     */
    @PutMapping("/{serieId}")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable UUID userId,
            @PathVariable Long serieId,
            @Valid @RequestBody UpdateStatusRequest updateStatusRequest) {
        serieWatchlistService.updateStatus(userId, serieId, null, updateStatusRequest.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Serie watch status updated", null, HttpStatus.OK));
    }

    /**
     * Remove a serie from watchlist
     */
    @DeleteMapping("/{serieId}")
    public ResponseEntity<ApiResponse<Void>> removeSerie(
            @PathVariable UUID userId,
            @PathVariable Long serieId) {
        serieWatchlistService.removeFromWatchlist(userId, serieId, null);
        return ResponseEntity.ok(ApiResponse.success("Serie removed from watchlist", null, HttpStatus.OK));
    }

}