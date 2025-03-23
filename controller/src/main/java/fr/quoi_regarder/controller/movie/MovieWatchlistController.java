package fr.quoi_regarder.controller.movie;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.dto.movie.MovieDto;
import fr.quoi_regarder.dto.movie.MovieWatchlistDto;
import fr.quoi_regarder.dto.movie.UpdateStatusRequest;
import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.service.movie.MovieWatchlistService;
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
@RequestMapping("/movie-watchlist/{userId}/movie")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("@userSecurity.checkUserId(#userId)")
public class MovieWatchlistController {
    private final MovieWatchlistService movieWatchlistService;

    /**
     * Get all movie IDs in a user's watchlist grouped by status
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<Long>>>> getWatchlist(
            @PathVariable UUID userId) {
        Map<String, List<Long>> result = movieWatchlistService.findWatchlistByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Movies found", result, HttpStatus.OK));
    }

    /**
     * Get detailed movie information from watchlist with pagination
     */
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<Page<MovieDto>>> getMovieDetails(
            @PathVariable UUID userId,
            @RequestParam WatchStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Page<MovieDto> movies = movieWatchlistService.findMoviesByUserIdAndStatus(userId, status, page, limit);
        return ResponseEntity.ok(ApiResponse.success("Movies found", movies, HttpStatus.OK));
    }

    /**
     * Get total runtime for user's watched movies
     */
    @GetMapping("/runtime")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTotalRuntime(
            @PathVariable UUID userId) {
        Long totalRuntime = movieWatchlistService.calculateTotalRuntimeForUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Total runtime found",
                Map.of("totalRuntime", totalRuntime), HttpStatus.OK));
    }

    /**
     * Add a movie to watchlist
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MovieWatchlistDto>> addMovie(
            @PathVariable UUID userId,
            @Valid @RequestBody MovieWatchlistDto movieWatchlistDTO) {
        MovieWatchlistDto result = movieWatchlistService.addMovieToWatchlist(userId, movieWatchlistDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Movie added to watchlist", result, HttpStatus.CREATED));
    }

    /**
     * Update movie watch status
     */
    @PutMapping("/{movieId}")
    public ResponseEntity<ApiResponse<MovieWatchlistDto>> updateStatus(
            @PathVariable UUID userId,
            @PathVariable Long movieId,
            @Valid @RequestBody UpdateStatusRequest updateStatusRequest) {

        MovieWatchlistDto result = movieWatchlistService.updateMovieStatus(
                userId, movieId, updateStatusRequest.getStatus());

        return ResponseEntity.ok(ApiResponse.success("Movie status updated", result, HttpStatus.OK));
    }

    /**
     * Remove a movie from watchlist
     */
    @DeleteMapping("/{movieId}")
    public ResponseEntity<ApiResponse<Void>> removeMovie(
            @PathVariable UUID userId,
            @PathVariable Long movieId) {

        movieWatchlistService.removeMovieFromWatchlist(userId, movieId);

        return ResponseEntity.ok(ApiResponse.success("Movie removed from watchlist", null, HttpStatus.OK));
    }
}