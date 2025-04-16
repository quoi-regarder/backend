package fr.quoi_regarder.service.movie.watchlist;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.dto.movie.MovieDto;
import fr.quoi_regarder.dto.movie.MovieWatchlistDto;
import fr.quoi_regarder.entity.movie.Movie;
import fr.quoi_regarder.entity.movie.MovieWatchlist;
import fr.quoi_regarder.entity.movie.id.MovieWatchlistId;
import fr.quoi_regarder.event.movie.MovieTotalRuntimeEvent;
import fr.quoi_regarder.event.movie.MovieWatchlistChangedEvent;
import fr.quoi_regarder.event.movie.MovieWatchlistIdsEvent;
import fr.quoi_regarder.exception.exceptions.EntityNotExistsException;
import fr.quoi_regarder.mapper.movie.MovieMapper;
import fr.quoi_regarder.mapper.movie.MovieWatchlistMapper;
import fr.quoi_regarder.repository.movie.MovieRepository;
import fr.quoi_regarder.repository.movie.MovieWatchlistRepository;
import fr.quoi_regarder.service.movie.MovieService;
import fr.quoi_regarder.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MovieWatchlistService {
    private final MovieWatchlistRepository movieWatchlistRepository;
    private final MovieWatchlistMapper movieWatchlistMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final MovieRepository movieRepository;
    private final MovieService movieService;
    private final MovieMapper movieMapper;
    private final UserService userService;

    /**
     * Finds all movies in user's watchlist categorized by status
     *
     * @param userId User ID
     * @return Map containing watched and to_watch movie IDs
     */
    public Map<String, List<Long>> findWatchlistByUserId(UUID userId) {
        Map<String, List<Long>> result = new HashMap<>();
        result.put(WatchStatus.watched.name(), movieWatchlistRepository.findMovieIdsByUserIdAndStatus(userId, WatchStatus.watched));
        result.put(WatchStatus.to_watch.name(), movieWatchlistRepository.findMovieIdsByUserIdAndStatus(userId, WatchStatus.to_watch));
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
        return totalRuntime != null ? totalRuntime : 0L;
    }

    /**
     * Adds a movie to user's watchlist
     *
     * @param userId User ID
     * @param dto    Movie watchlist data
     * @return Created watchlist entry
     */
    @Transactional
    public MovieWatchlistDto addMovieToWatchlist(UUID userId, MovieWatchlistDto dto) {
        MovieWatchlistDto result = create(dto);

        // Publish an event that will be processed after transaction commit
        eventPublisher.publishEvent(new MovieWatchlistChangedEvent(this, userId));

        return result;
    }

    /**
     * Updates movie status
     *
     * @param userId User ID
     * @param tmdbId TMDB movie ID
     * @param status New watch status
     * @return Updated watchlist entry
     */
    @Transactional
    public MovieWatchlistDto updateMovieStatus(UUID userId, Long tmdbId, WatchStatus status) {
        MovieWatchlistDto result = update(userId, tmdbId, status);

        // Publish an event that will be processed after transaction commit
        eventPublisher.publishEvent(new MovieWatchlistChangedEvent(this, userId));

        return result;
    }

    /**
     * Removes a movie from user's watchlist
     *
     * @param userId User ID
     * @param tmdbId TMDB movie ID
     */
    @Transactional
    public void removeMovieFromWatchlist(UUID userId, Long tmdbId) {
        delete(userId, tmdbId);

        // Publish an event that will be processed after transaction commit
        eventPublisher.publishEvent(new MovieWatchlistChangedEvent(this, userId));
    }

    /**
     * Creates a new movie watchlist entry
     *
     * @param dto MovieWatchlistDto
     * @return Created MovieWatchlistDto
     */
    private MovieWatchlistDto create(MovieWatchlistDto dto) {
        movieService.ensureMovieExists(dto.getTmdbId());

        MovieWatchlistId movieWatchlistId = new MovieWatchlistId();
        movieWatchlistId.setUserId(dto.getUserId());
        movieWatchlistId.setTmdbId(dto.getTmdbId());
        Optional<MovieWatchlist> existing = movieWatchlistRepository.findById(movieWatchlistId);

        MovieWatchlist entity = existing.orElseGet(() -> {
            MovieWatchlist newEntry = new MovieWatchlist();
            newEntry.setId(movieWatchlistId);

            return newEntry;
        });

        entity.setStatus(dto.getStatus());
        return movieWatchlistMapper.toDto(movieWatchlistRepository.save(entity));
    }


    /**
     * Updates a movie watchlist entry
     *
     * @param userId User ID
     * @param tmdbId TMDB ID
     * @param status New watch status
     * @return Updated MovieWatchlistDto
     */
    private MovieWatchlistDto update(UUID userId, Long tmdbId, WatchStatus status) {
        MovieWatchlist movieWatchlist = movieWatchlistRepository
                .findByIdTmdbIdAndIdUserId(tmdbId, userId)
                .orElseThrow(() -> new EntityNotExistsException(MovieWatchlist.class,
                        String.format("User ID: %s, TMDB ID: %s", userId, tmdbId)));

        movieWatchlist.setStatus(status);
        return movieWatchlistMapper.toDto(movieWatchlistRepository.save(movieWatchlist));
    }

    /**
     * Deletes a movie watchlist entry
     *
     * @param userId User ID
     * @param tmdbId TMDB ID
     */
    private void delete(UUID userId, Long tmdbId) {
        movieWatchlistRepository.deleteByUserIdAndTmdbId(userId, tmdbId);
    }

    /**
     * Event handler that executes after transaction commit
     * and sends notifications with up-to-date data.
     *
     * @param event Event indicating that a watchlist has been modified
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWatchlistChangedEvent(MovieWatchlistChangedEvent event) {
        UUID userId = event.getUserId();

        try {
            // Retrieve fresh data
            Map<String, List<Long>> watchlist = findWatchlistByUserId(userId);
            Long totalRuntime = calculateTotalRuntimeForUser(userId);

            // Publish events to clients
            eventPublisher.publishEvent(new MovieWatchlistIdsEvent(this, userId, watchlist));

            Map<String, Long> runtimeData = Map.of("totalRuntime", totalRuntime);
            eventPublisher.publishEvent(new MovieTotalRuntimeEvent(this, userId, runtimeData));
        } catch (Exception ignored) {
        }
    }
}