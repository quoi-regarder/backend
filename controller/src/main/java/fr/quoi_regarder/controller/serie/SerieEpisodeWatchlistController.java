package fr.quoi_regarder.controller.serie;

import fr.quoi_regarder.dto.movie.UpdateStatusRequest;
import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.dto.serie.SerieEpisodeWatchlistDto;
import fr.quoi_regarder.service.serie.watchlist.SerieEpisodeWatchlistService;
import fr.quoi_regarder.service.serie.watchlist.SerieWatchlistEventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/serie-watchlist/{userId}/serie/{serieId}/episode")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class SerieEpisodeWatchlistController {
    private final SerieEpisodeWatchlistService serieEpisodeWatchlistService;
    private final SerieWatchlistEventService serieWatchlistEventService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<Long>>>> getEpisodeWatchlist(
            @PathVariable UUID userId,
            @PathVariable Long serieId
    ) {
        Map<String, List<Long>> result = serieWatchlistEventService.findEpisodeWatchlistByUserId(userId, serieId);
        return ResponseEntity.ok(ApiResponse.success("Episode watchlist found", result, HttpStatus.OK));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addEpisode(
            @PathVariable UUID userId,
            @PathVariable Long serieId,
            @Valid @RequestBody SerieEpisodeWatchlistDto dto
    ) {
        serieEpisodeWatchlistService.addToWatchlist(userId, serieId, dto.getTmdbId(), dto.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Episode added to watchlist", null, HttpStatus.OK));
    }

    @PutMapping("/{episodeId}")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable UUID userId,
            @PathVariable Long serieId,
            @PathVariable Long episodeId,
            @Valid @RequestBody UpdateStatusRequest updateStatusRequest
    ) {
        serieEpisodeWatchlistService.updateWatchStatus(userId, serieId, episodeId, updateStatusRequest.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Episode watch status updated", null, HttpStatus.OK));
    }

    @DeleteMapping("/{episodeId}")
    public ResponseEntity<ApiResponse<Void>> removeEpisode(
            @PathVariable UUID userId,
            @PathVariable Long serieId,
            @PathVariable Long episodeId
    ) {
        serieEpisodeWatchlistService.removeFromWatchlist(userId, serieId, episodeId);
        return ResponseEntity.ok(ApiResponse.success("Episode removed from watchlist", null, HttpStatus.OK));
    }
}