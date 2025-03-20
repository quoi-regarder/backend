package fr.quoi_regarder.service;

import fr.quoi_regarder.commons.enums.LanguageIsoType;
import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.dto.movie.MovieDto;
import fr.quoi_regarder.dto.movie.MovieWatchlistDto;
import fr.quoi_regarder.entity.movie.Movie;
import fr.quoi_regarder.entity.movie.MovieTranslation;
import fr.quoi_regarder.entity.movie.MovieWatchlist;
import fr.quoi_regarder.entity.movie.id.MovieTranslationId;
import fr.quoi_regarder.entity.movie.id.MovieWatchlistId;
import fr.quoi_regarder.event.MovieTotalRuntimeEvent;
import fr.quoi_regarder.event.MovieWatchlistIdsEvent;
import fr.quoi_regarder.exception.exceptions.EntityNotExistsException;
import fr.quoi_regarder.mapper.movie.MovieMapper;
import fr.quoi_regarder.mapper.movie.MovieWatchlistMapper;
import fr.quoi_regarder.repository.movie.MovieRepository;
import fr.quoi_regarder.repository.movie.MovieTranslationRepository;
import fr.quoi_regarder.repository.movie.MovieWatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.*;

/**
 * Service responsible for managing movie watchlists
 */
@Service
@RequiredArgsConstructor
public class MovieWatchlistService {
    private final MovieTranslationRepository movieTranslationRepository;
    private final MovieWatchlistRepository movieWatchlistRepository;
    private final MovieWatchlistMapper movieWatchlistMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final TmdbService tmdbService;
    private final UserService userService;

    /**
     * Finds all movies in user's watchlist categorized by status
     *
     * @param userId User ID
     * @return Map containing watched and to_watch movie IDs
     */
    public Map<String, List<Long>> findWatchlistByUserId(UUID userId) {
        Map<String, List<Long>> result = new HashMap<>();
        result.put("watched", movieWatchlistRepository.findMovieIdsByUserIdAndStatus(userId, WatchStatus.watched));
        result.put("to_watch", movieWatchlistRepository.findMovieIdsByUserIdAndStatus(userId, WatchStatus.to_watch));

        // Publier l'événement de manière asynchrone
        publishWatchlistEventAsync(userId, result);

        return result;
    }

    /**
     * Finds movies by user ID and watch status with pagination
     *
     * @param userId User ID
     * @param status Watch status
     * @param page   Page number
     * @param limit  Items per page
     * @return Page of MovieDto
     */
    public Page<MovieDto> findMoviesByUserIdAndStatus(UUID userId, WatchStatus status, int page, int limit) {
        String userLanguage = userService.getCurrentUserLanguage();

        Page<Movie> movies = movieRepository.findByWatchlistUserIdAndWatchlistStatus(
                userId, status, PageRequest.of(page, limit));

        return movies.map(movie -> movieMapper.toDto(movie, userLanguage));
    }

    /**
     * Calculates total runtime for watched movies
     *
     * @param userId User ID
     * @return Total runtime in minutes
     */
    public Long calculateTotalRuntimeForUser(UUID userId) {
        Long totalRuntime = movieWatchlistRepository.getTotalRuntimeForUserAndStatus(userId, WatchStatus.watched);
        totalRuntime = totalRuntime != null ? totalRuntime : 0L;

        // Publier l'événement de manière asynchrone
        publishTotalRuntimeEventAsync(userId, totalRuntime);

        return totalRuntime;
    }

    /**
     * Adds a movie to user's watchlist and refreshes user stats
     *
     * @param userId User ID
     * @param dto    Movie watchlist data
     * @return Created watchlist entry
     */
    @Transactional
    public MovieWatchlistDto addMovieToWatchlist(UUID userId, MovieWatchlistDto dto) {
        MovieWatchlistDto result = create(dto);

        // Refresh user stats asynchronously
        refreshUserStatsAsync(userId);

        return result;
    }

    /**
     * Creates a new movie watchlist entry
     *
     * @param dto MovieWatchlistDto
     * @return Created MovieWatchlistDto
     */
    @Transactional
    public MovieWatchlistDto create(MovieWatchlistDto dto) {
        ensureMovieExists(dto.getTmdbId());

        MovieWatchlist movieWatchlist = new MovieWatchlist();
        MovieWatchlistId movieWatchlistId = new MovieWatchlistId();
        movieWatchlistId.setUserId(dto.getUserId());
        movieWatchlistId.setTmdbId(dto.getTmdbId());

        movieWatchlist.setId(movieWatchlistId);
        movieWatchlist.setStatus(dto.getStatus());

        return movieWatchlistMapper.toDto(movieWatchlistRepository.save(movieWatchlist));
    }

    /**
     * Updates movie status and refreshes user stats
     *
     * @param userId User ID
     * @param tmdbId TMDB movie ID
     * @param status New watch status
     * @return Updated watchlist entry
     */
    @Transactional
    public MovieWatchlistDto updateMovieStatus(UUID userId, Long tmdbId, WatchStatus status) {
        MovieWatchlistDto result = update(userId, tmdbId, status);

        // Refresh user stats asynchronously
        refreshUserStatsAsync(userId);

        return result;
    }

    /**
     * Updates a movie watchlist entry
     *
     * @param userId User ID
     * @param tmdbId TMDB ID
     * @param status New watch status
     * @return Updated MovieWatchlistDto
     */
    @Transactional
    public MovieWatchlistDto update(UUID userId, Long tmdbId, WatchStatus status) {
        MovieWatchlist movieWatchlist = movieWatchlistRepository
                .findByIdTmdbIdAndIdUserId(tmdbId, userId)
                .orElseThrow(() -> new EntityNotExistsException(MovieWatchlist.class,
                        String.format("User ID: %s, TMDB ID: %s", userId, tmdbId)));

        movieWatchlist.setStatus(status);
        return movieWatchlistMapper.toDto(movieWatchlistRepository.save(movieWatchlist));
    }

    /**
     * Removes a movie from user's watchlist and refreshes user stats
     *
     * @param userId User ID
     * @param tmdbId TMDB movie ID
     */
    @Transactional
    public void removeMovieFromWatchlist(UUID userId, Long tmdbId) {
        delete(userId, tmdbId);

        // Refresh user stats asynchronously
        refreshUserStatsAsync(userId);
    }

    /**
     * Deletes a movie watchlist entry
     *
     * @param userId User ID
     * @param tmdbId TMDB ID
     */
    @Transactional
    public void delete(UUID userId, Long tmdbId) {
        MovieWatchlistId id = new MovieWatchlistId();
        id.setUserId(userId);
        id.setTmdbId(tmdbId);

        movieWatchlistRepository.deleteById(id);
    }

    /**
     * Ensures movie exists in the database, fetches and saves it if necessary
     *
     * @param tmdbId TMDB ID
     */
    private void ensureMovieExists(Long tmdbId) {
        String userLanguage = userService.getCurrentUserLanguage();
        Map<String, Object> movieDetails = tmdbService.fetchMovieDetails(tmdbId, userLanguage);

        // Save or update movie details
        saveOrUpdateMovie(tmdbId, movieDetails);

        // Save or update movie translation for user's language
        saveMovieTranslation(tmdbId, userLanguage, movieDetails);

        // Fetch and save translations for other languages asynchronously
        fetchOtherLanguageTranslationsAsync(tmdbId, userLanguage);
    }

    /**
     * Saves or updates movie details
     *
     * @param tmdbId       TMDB ID
     * @param movieDetails Movie details from TMDB
     */
    private void saveOrUpdateMovie(Long tmdbId, Map<String, Object> movieDetails) {
        Movie movie = movieRepository.findByTmdbId(tmdbId).orElse(new Movie());
        movie.setTmdbId(tmdbId);
        movie.setRuntime((Integer) movieDetails.get("runtime"));
        movie.setReleaseDate(Date.valueOf((String) movieDetails.get("release_date")));
        movie.setPosterPath((String) movieDetails.get("poster_path"));
        movieRepository.save(movie);
    }

    /**
     * Saves movie translation for a specific language
     *
     * @param tmdbId       TMDB ID
     * @param language     Language code
     * @param movieDetails Movie details from TMDB
     */
    private void saveMovieTranslation(Long tmdbId, String language, Map<String, Object> movieDetails) {
        MovieTranslation translation = movieTranslationRepository
                .findByIdTmdbIdAndIdLanguage(tmdbId, language)
                .orElse(new MovieTranslation());

        MovieTranslationId id = new MovieTranslationId();
        id.setTmdbId(tmdbId);
        id.setLanguage(language);

        translation.setId(id);
        translation.setTitle((String) movieDetails.get("title"));
        translation.setOverview((String) movieDetails.get("overview"));
        movieTranslationRepository.save(translation);
    }

    /**
     * Refreshes user watchlist stats asynchronously
     *
     * @param userId User ID
     */
    @Async
    public void refreshUserStatsAsync(UUID userId) {
        findWatchlistByUserId(userId);
        calculateTotalRuntimeForUser(userId);
    }

    /**
     * Publishes watchlist IDs event asynchronously
     *
     * @param userId        User ID
     * @param watchlistData Watchlist data to publish
     */
    @Async
    public void publishWatchlistEventAsync(UUID userId, Map<String, List<Long>> watchlistData) {
        eventPublisher.publishEvent(new MovieWatchlistIdsEvent(this, userId, watchlistData));
    }

    /**
     * Publishes total runtime event asynchronously
     *
     * @param userId       User ID
     * @param totalRuntime Total runtime value
     */
    @Async
    public void publishTotalRuntimeEventAsync(UUID userId, Long totalRuntime) {
        Map<String, Long> result = Map.of("totalRuntime", totalRuntime);
        try {
            eventPublisher.publishEvent(new MovieTotalRuntimeEvent(this, userId, result));
        } catch (Exception e) {
            // Exception silencieusement ignorée
        }
    }

    /**
     * Asynchronously fetch and save translations for other languages
     *
     * @param tmdbId          TMDB ID
     * @param defaultLanguage Default language to exclude
     */
    @Async
    public void fetchOtherLanguageTranslationsAsync(Long tmdbId, String defaultLanguage) {
        LanguageIsoType defaultLangType = LanguageIsoType.findByCode(defaultLanguage);

        Arrays.stream(LanguageIsoType.values())
                .filter(lang -> !lang.equals(defaultLangType))
                .forEach(lang -> {
                    Map<String, Object> movieDetails = tmdbService.fetchMovieDetails(tmdbId, lang.getCode());
                    saveMovieTranslation(tmdbId, lang.getCode(), movieDetails);
                });
    }
}