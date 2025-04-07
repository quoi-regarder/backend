package fr.quoi_regarder.service.movie;

import fr.quoi_regarder.dto.movie.MovieDto;
import fr.quoi_regarder.entity.movie.Movie;
import fr.quoi_regarder.entity.movie.MovieFavorite;
import fr.quoi_regarder.entity.movie.MovieFavoriteDto;
import fr.quoi_regarder.entity.movie.id.MovieFavoriteId;
import fr.quoi_regarder.event.movie.MovieFavoriteChangedEvent;
import fr.quoi_regarder.event.movie.MovieFavoriteIdsEvent;
import fr.quoi_regarder.mapper.movie.MovieFavoriteMapper;
import fr.quoi_regarder.mapper.movie.MovieMapper;
import fr.quoi_regarder.repository.movie.MovieFavoriteRepository;
import fr.quoi_regarder.repository.movie.MovieRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovieFavoriteService {
    private final MovieFavoriteRepository movieFavoriteRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MovieFavoriteMapper movieFavoriteMapper;
    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final UserService userService;
    private final MovieService movieService;

    /**
     * Finds all movie IDs in a user's favorites
     *
     * @param userId User ID
     * @return List of movie IDs
     */
    public Map<String, List<Long>> findFavoriteMovieIdsByUserId(UUID userId) {
        Map<String, List<Long>> result = new HashMap<>();
        result.put("favorite", movieFavoriteRepository.findMovieIdsByUserId(userId));

        return result;
    }

    /**
     * Finds movies by user ID with pagination
     *
     * @param userId User ID
     * @param page   Page number
     * @param limit  Items per page
     * @return Page of MovieDto
     */
    public Page<MovieDto> findMoviesByUserId(UUID userId, int page, int limit) {
        String userLanguage = userService.getCurrentUserLanguage();

        Page<Movie> movies = movieRepository.findByFavoriteUserId(
                userId, PageRequest.of(page, limit));

        return movies.map(movie -> movieMapper.toDto(movie, userLanguage));
    }

    /**
     * Adds a movie to favorites
     *
     * @param userId           User ID
     * @param movieFavoriteDto MovieFavoriteDto
     * @return MovieFavoriteDto
     */
    @Transactional
    public MovieFavoriteDto addMovieToFavorites(UUID userId, MovieFavoriteDto movieFavoriteDto) {
        MovieFavoriteDto result = create(movieFavoriteDto);

        eventPublisher.publishEvent(new MovieFavoriteChangedEvent(this, userId));

        return result;
    }

    /**
     * Removes a movie from favorites
     *
     * @param userId User ID
     * @param tmdbId TMDB ID
     */
    @Transactional
    public void removeMovieFromFavorites(UUID userId, Long tmdbId) {
        delete(userId, tmdbId);

        eventPublisher.publishEvent(new MovieFavoriteChangedEvent(this, userId));
    }

    /**
     * Creates a new MovieFavorite entity
     *
     * @param movieFavoriteDto MovieFavoriteDto
     * @return MovieFavoriteDto
     */
    private MovieFavoriteDto create(MovieFavoriteDto movieFavoriteDto) {
        movieService.ensureMovieExists(movieFavoriteDto.getTmdbId());

        MovieFavoriteId movieFavoriteId = new MovieFavoriteId();
        movieFavoriteId.setUserId(movieFavoriteDto.getUserId());
        movieFavoriteId.setTmdbId(movieFavoriteDto.getTmdbId());

        if (movieFavoriteRepository.existsById(movieFavoriteId)) {
            return movieFavoriteDto;
        }

        MovieFavorite movieFavorite = new MovieFavorite();
        movieFavorite.setId(movieFavoriteId);

        return movieFavoriteMapper.toDto(movieFavoriteRepository.save(movieFavorite));
    }

    /**
     * Deletes a MovieFavorite entity
     *
     * @param userId User ID
     * @param tmdbId TMDB ID
     */
    private void delete(UUID userId, Long tmdbId) {
        movieFavoriteRepository.deleteByUserIdAndTmdbId(userId, tmdbId);
    }

    /**
     * Handles the event when a movie is added to or removed from favorites
     * and sends notifications with up-to-date data.
     *
     * @param event Event indicating a change in movie favorites
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMovieFavoriteChangedEvent(MovieFavoriteChangedEvent event) {
        UUID userId = event.getUserId();

        try {
            Map<String, List<Long>> movieIds = findFavoriteMovieIdsByUserId(userId);

            eventPublisher.publishEvent(new MovieFavoriteIdsEvent(this, userId, movieIds));
        } catch (Exception ignored) {
        }
    }
}
