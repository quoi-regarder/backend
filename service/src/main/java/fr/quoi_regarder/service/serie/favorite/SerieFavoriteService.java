package fr.quoi_regarder.service.serie.favorite;

import fr.quoi_regarder.dto.serie.SerieDto;
import fr.quoi_regarder.dto.serie.SerieFavoriteDto;
import fr.quoi_regarder.entity.serie.Serie;
import fr.quoi_regarder.entity.serie.SerieFavorite;
import fr.quoi_regarder.entity.serie.id.SerieFavoriteId;
import fr.quoi_regarder.event.serie.SerieFavoriteIdsEvent;
import fr.quoi_regarder.mapper.serie.SerieFavoriteMapper;
import fr.quoi_regarder.mapper.serie.SerieMapper;
import fr.quoi_regarder.repository.serie.SerieFavoriteRepository;
import fr.quoi_regarder.repository.serie.SerieRepository;
import fr.quoi_regarder.service.serie.SerieService;
import fr.quoi_regarder.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SerieFavoriteService {
    private final SerieFavoriteRepository serieFavoriteRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SerieFavoriteMapper serieFavoriteMapper;
    private final SerieRepository serieRepository;
    private final SerieService serieService;
    private final UserService userService;
    private final SerieMapper serieMapper;

    /**
     * Finds all movie IDs in a user's favorites
     *
     * @param userId User ID
     * @return List of movie IDs
     */
    public Map<String, List<Long>> findFavoriteMovieIdsByUserId(UUID userId) {
        Map<String, List<Long>> result = new HashMap<>();
        result.put("favorite", serieFavoriteRepository.findSerieIdsByUserId(userId));

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
    public Page<SerieDto> findMoviesByUserId(UUID userId, int page, int limit) {
        String userLanguage = userService.getCurrentUserLanguage();

        Page<Serie> series = serieRepository.findByFavoriteUserId(
                userId, org.springframework.data.domain.PageRequest.of(page, limit));

        return series.map(serie -> serieMapper.toDto(serie, userLanguage));
    }

    /**
     * Adds a movie to favorites
     *
     * @param userId           User ID
     * @param serieFavoriteDto Movie favorite DTO
     */
    @Transactional
    public void addMovieToFavorites(UUID userId, SerieFavoriteDto serieFavoriteDto) {
        serieService.loadSerie(userId, serieFavoriteDto.getTmdbId(), (uid, sid) -> {
            create(serieFavoriteDto);

            publishMovieFavoriteChangedEvent(userId);
        });
    }

    /**
     * Removes a movie from favorites
     *
     * @param userId  User ID
     * @param movieId Movie ID
     */
    @Transactional
    public void removeMovieFromFavorites(UUID userId, Long movieId) {
        serieService.loadSerie(userId, movieId, (uid, sid) -> {
            delete(userId, movieId);

            publishMovieFavoriteChangedEvent(userId);
        });
    }

    /**
     * Publishes an event when a movie favorite is changed
     *
     * @param serieFavoriteDto Movie favorite DTO
     */
    private void create(SerieFavoriteDto serieFavoriteDto) {
        SerieFavoriteId serieFavoriteId = new SerieFavoriteId();
        serieFavoriteId.setUserId(serieFavoriteDto.getUserId());
        serieFavoriteId.setTmdbId(serieFavoriteDto.getTmdbId());

        if (serieFavoriteRepository.existsById(serieFavoriteId)) {
            return;
        }

        SerieFavorite serieFavorite = new SerieFavorite();
        serieFavorite.setId(serieFavoriteId);

        serieFavoriteMapper.toDto(serieFavoriteRepository.save(serieFavorite));
    }

    /**
     * Publishes an event when a movie favorite is changed
     *
     * @param userId User ID
     * @param tmdbId Movie ID
     */
    private void delete(UUID userId, Long tmdbId) {
        SerieFavoriteId serieFavoriteId = new SerieFavoriteId();
        serieFavoriteId.setUserId(userId);
        serieFavoriteId.setTmdbId(tmdbId);

        serieFavoriteRepository.deleteById(serieFavoriteId);
    }

    /**
     * Publishes an event when a movie favorite is changed
     *
     * @param userId User ID
     */
    public void publishMovieFavoriteChangedEvent(UUID userId) {
        try {
            Map<String, List<Long>> result = findFavoriteMovieIdsByUserId(userId);

            eventPublisher.publishEvent(new SerieFavoriteIdsEvent(this, userId, result));
        } catch (Exception ignore) {
        }
    }
}
