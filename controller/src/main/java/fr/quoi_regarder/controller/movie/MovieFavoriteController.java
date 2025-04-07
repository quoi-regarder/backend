package fr.quoi_regarder.controller.movie;

import fr.quoi_regarder.dto.movie.MovieDto;
import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.entity.movie.MovieFavoriteDto;
import fr.quoi_regarder.service.movie.MovieFavoriteService;
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
@RequestMapping("/movie-favorite/{userId}/movie")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("@userSecurity.checkUserId(#userId)")
public class MovieFavoriteController {
    private final MovieFavoriteService movieFavoriteService;

    /**
     * Get all movie IDs in a user's favorites
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, List<Long>>>> getFavoriteMovies(
            @PathVariable UUID userId
    ) {
        Map<String, List<Long>> result = movieFavoriteService.findFavoriteMovieIdsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Movies found", result, HttpStatus.OK));
    }

    /**
     * Get detailed movie information from favorites with pagination
     */
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<Page<MovieDto>>> getMovieDetails(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<MovieDto> movies = movieFavoriteService.findMoviesByUserId(userId, page, limit);
        return ResponseEntity.ok(ApiResponse.success("Movies found", movies, HttpStatus.OK));
    }

    /**
     * Add a movie to favorites
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MovieFavoriteDto>> addFavoriteMovie(
            @PathVariable UUID userId,
            @Valid @RequestBody MovieFavoriteDto movieFavoriteDto
    ) {
        MovieFavoriteDto result = movieFavoriteService.addMovieToFavorites(userId, movieFavoriteDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Movie added to favorites", result, HttpStatus.CREATED));
    }

    /**
     * Remove a movie from favorites
     */
    @DeleteMapping("/{movieId}")
    public ResponseEntity<ApiResponse<Void>> removeFavoriteMovie(
            @PathVariable UUID userId,
            @PathVariable Long movieId
    ) {
        movieFavoriteService.removeMovieFromFavorites(userId, movieId);

        return ResponseEntity.ok(ApiResponse.success("Movie removed from favorites", null, HttpStatus.OK));
    }
}
