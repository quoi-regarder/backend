package fr.quoi_regarder.controller.serie;


import fr.quoi_regarder.dto.movie.UpdateStatusRequest;
import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.dto.serie.SerieSeasonWatchlistDto;
import fr.quoi_regarder.service.serie.SerieSeasonWatchlistService;
import fr.quoi_regarder.service.serie.SerieWatchlistEventService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/serie-watchlist/{userId}/serie/{serieId}/season")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("@userSecurity.checkUserId(#userId)")
public class SerieSeasonWatchlistController {
    private final SerieSeasonWatchlistService serieSeasonWatchlistService;
    private final SerieWatchlistEventService serieWatchlistEventService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<Long>>>> getSerieSeasons(
            @PathVariable UUID userId,
            @PathVariable String serieId
    ) {
        Map<String, List<Long>> result = serieWatchlistEventService.findSeasonWatchlistByUserId(userId, Long.valueOf(serieId));

        return ResponseEntity.ok(ApiResponse.success("Seasons found", result, HttpStatus.OK));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addSeason(
            @PathVariable UUID userId,
            @PathVariable String serieId,
            @Valid @RequestBody SerieSeasonWatchlistDto dto
    ) {
        serieSeasonWatchlistService.addToWatchlist(userId, Long.valueOf(serieId), dto);
        return ResponseEntity.ok(ApiResponse.success("Season added to watchlist", null, HttpStatus.OK));
    }

    @PutMapping("/{seasonId}")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable UUID userId,
            @PathVariable String serieId,
            @PathVariable String seasonId,
            @Valid @RequestBody UpdateStatusRequest updateStatusRequest
    ) {
        serieSeasonWatchlistService.updateStatus(userId, Long.valueOf(serieId), Long.valueOf(seasonId), updateStatusRequest.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Season watch status updated", null, HttpStatus.OK));
    }

    @DeleteMapping("/{seasonId}")
    public ResponseEntity<ApiResponse<Void>> removeSeason(
            @PathVariable UUID userId,
            @PathVariable String serieId,
            @PathVariable String seasonId
    ) {
        serieSeasonWatchlistService.removeFromWatchlist(userId, Long.valueOf(serieId), Long.valueOf(seasonId));
        return ResponseEntity.ok(ApiResponse.success("Season removed from watchlist", null, HttpStatus.OK));
    }
}
