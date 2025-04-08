package fr.quoi_regarder.controller.serie;

import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.dto.serie.SerieDto;
import fr.quoi_regarder.dto.serie.SerieFavoriteDto;
import fr.quoi_regarder.service.serie.favorite.SerieFavoriteService;
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
@RequestMapping("/serie-favorite/{userId}/serie")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("@userSecurity.checkUserId(#userId)")
public class SerieFavoriteController {
    private final SerieFavoriteService serieFavoriteService;

    /**
     * Get all movie IDs in a user's favorites
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<Long>>>> getFavoriteMovies(
            @PathVariable UUID userId
    ) {
        Map<String, List<Long>> result = serieFavoriteService.findFavoriteMovieIdsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Movies found", result, HttpStatus.OK));
    }

    /**
     * Get detailed movie information from favorites with pagination
     */
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<Page<SerieDto>>> getFavoriteMovies(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Page<SerieDto> movies = serieFavoriteService.findMoviesByUserId(userId, page, limit);
        return ResponseEntity.ok(ApiResponse.success("Movies found", movies, HttpStatus.OK));
    }

    /**
     * Add a movie to favorites
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addFavoriteMovie(
            @PathVariable UUID userId,
            @Valid @RequestBody SerieFavoriteDto serieFavoriteDto
    ) {
        serieFavoriteService.addMovieToFavorites(userId, serieFavoriteDto);

        return ResponseEntity.ok(ApiResponse.success("Movie added to favorites", null, HttpStatus.OK));
    }

    /**
     * Remove a movie from favorites
     */
    @DeleteMapping("/{movieId}")
    public ResponseEntity<ApiResponse<Void>> removeFavoriteMovie(
            @PathVariable UUID userId,
            @PathVariable Long movieId
    ) {
        serieFavoriteService.removeMovieFromFavorites(userId, movieId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("Movie removed from favorites", null, HttpStatus.NO_CONTENT));
    }
}
