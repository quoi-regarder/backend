package fr.quoi_regarder.service.movie;

import fr.quoi_regarder.commons.enums.LanguageIsoType;
import fr.quoi_regarder.entity.movie.Movie;
import fr.quoi_regarder.entity.movie.MovieTranslation;
import fr.quoi_regarder.entity.movie.id.MovieTranslationId;
import fr.quoi_regarder.exception.exceptions.EntityNotExistsException;
import fr.quoi_regarder.repository.movie.MovieRepository;
import fr.quoi_regarder.repository.movie.MovieTranslationRepository;
import fr.quoi_regarder.service.TmdbService;
import fr.quoi_regarder.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieTranslationRepository movieTranslationRepository;
    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;
    private final UserService userService;

    /**
     * Ensures movie exists in the database, fetches and saves it if necessary
     *
     * @param tmdbId TMDB ID
     */
    @Transactional
    public void ensureMovieExists(Long tmdbId) {
        if (movieRepository.existsById(tmdbId)) {
            return; // Skip if movie already exists
        }

        String userLanguage = userService.getCurrentUserLanguage();
        Map<String, Object> movieDetails = tmdbService.fetchMovieDetails(tmdbId, userLanguage);

        if (movieDetails == null || movieDetails.isEmpty()) {
            throw new EntityNotExistsException(Movie.class, String.format("TMDB ID: %s", tmdbId));
        }

        // Save or update movie details and translations
        saveOrUpdateMovie(tmdbId, movieDetails);
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

        // Vérifier si release_date est null avant la conversion
        String releaseDate = (String) movieDetails.get("release_date");
        if (releaseDate != null && !releaseDate.isEmpty()) {
            movie.setReleaseDate(Date.valueOf(releaseDate));
        } else {
            movie.setReleaseDate(null);
        }

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
        // Verify if the translation already exists
        MovieTranslationId id = new MovieTranslationId();
        id.setTmdbId(tmdbId);
        id.setLanguage(language);
        if (movieTranslationRepository.existsById(id)) {
            return;
        }

        MovieTranslation translation = new MovieTranslation();

        translation.setId(id);
        translation.setTitle((String) movieDetails.get("title"));
        translation.setOverview((String) movieDetails.get("overview"));
        movieTranslationRepository.save(translation);
    }

    /**
     * Asynchronously fetch and save translations for other languages
     *
     * @param tmdbId          TMDB ID
     * @param defaultLanguage Default language to exclude
     */
    @Async
    public void fetchOtherLanguageTranslationsAsync(Long tmdbId, String defaultLanguage) {
        CompletableFuture.runAsync(() -> {
            LanguageIsoType defaultLangType = LanguageIsoType.findByCode(defaultLanguage);

            Arrays.stream(LanguageIsoType.values())
                    .filter(lang -> !lang.equals(defaultLangType))
                    .parallel()
                    .forEach(lang -> {
                        // Verify if the translation already exists before making the API call
                        MovieTranslationId id = new MovieTranslationId();
                        id.setTmdbId(tmdbId);
                        id.setLanguage(lang.getCode());
                        if (!movieTranslationRepository.existsById(id)) {
                            Map<String, Object> movieDetails = tmdbService.fetchMovieDetails(tmdbId, lang.getCode());
                            saveMovieTranslation(tmdbId, lang.getCode(), movieDetails);
                        }
                    });
        });
    }
}
