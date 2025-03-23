package fr.quoi_regarder.service.serie;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.event.serie.SerieEpisodeWatchlistIdsEvent;
import fr.quoi_regarder.event.serie.SerieSeasonWatchlistIdsEvent;
import fr.quoi_regarder.event.serie.SerieTotalRuntimeEvent;
import fr.quoi_regarder.event.serie.SerieWatchlistIdsEvent;
import fr.quoi_regarder.repository.serie.SerieEpisodeWatchlistRepository;
import fr.quoi_regarder.repository.serie.SerieSeasonWatchlistRepository;
import fr.quoi_regarder.repository.serie.SerieWatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SerieWatchlistEventService {
    private final SerieEpisodeWatchlistRepository serieEpisodeWatchlistRepository;
    private final SerieSeasonWatchlistRepository serieSeasonWatchlistRepository;
    private final SerieWatchlistRepository serieWatchlistRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void publishWatchlistEvents(UUID userId, Long serieId) {
        // Publish serie watchlist IDs
        Map<String, List<Long>> serieWatchlistIds = findWatchlistByUserId(userId);
        eventPublisher.publishEvent(new SerieWatchlistIdsEvent(this, userId, serieWatchlistIds));

        // Publish season watchlist IDs
        Map<String, List<Long>> seasonWatchlistIds = findSeasonWatchlistByUserId(userId, serieId);
        eventPublisher.publishEvent(new SerieSeasonWatchlistIdsEvent(this, userId, seasonWatchlistIds));

        // Publish episode watchlist IDs
        Map<String, List<Long>> episodeWatchlistIds = findEpisodeWatchlistByUserId(userId, serieId);
        eventPublisher.publishEvent(new SerieEpisodeWatchlistIdsEvent(this, userId, episodeWatchlistIds));

        // Publish total runtime
        Map<String, Long> totalRuntime = getTotalRuntimeForUser(userId);
        eventPublisher.publishEvent(new SerieTotalRuntimeEvent(this, userId, totalRuntime));
    }

    /**
     * Get total runtime for a user
     */
    public Map<String, Long> getTotalRuntimeForUser(UUID userId) {
        Long totalRuntime = serieEpisodeWatchlistRepository.calculateTotalRuntimeForUser(userId);
        return Map.of("totalRuntime", totalRuntime != null ? totalRuntime : 0L);
    }

    /**
     * Gets user's series watchlist ids by status
     */
    public Map<String, List<Long>> findWatchlistByUserId(UUID userId) {
        Map<String, List<Long>> result = new HashMap<>();
        for (WatchStatus status : WatchStatus.values()) {
            List<Long> ids = serieWatchlistRepository.findSerieIdsByUserIdAndStatus(userId, status);
            result.put(status.name(), ids != null ? ids : Collections.emptyList());
        }
        return result;
    }

    /**
     * Gets user's season watchlist ids by status
     */
    public Map<String, List<Long>> findSeasonWatchlistByUserId(UUID userId, Long serieTmdbId) {
        Map<String, List<Long>> result = new HashMap<>();
        for (WatchStatus status : WatchStatus.values()) {
            List<Long> ids = serieSeasonWatchlistRepository.findSeasonByUserIdAndSerieTmdbIdAndStatus(userId, serieTmdbId, status);
            result.put(status.name(), ids != null ? ids : Collections.emptyList());
        }
        return result;
    }

    /**
     * Gets user's episode watchlist ids by status
     */
    public Map<String, List<Long>> findEpisodeWatchlistByUserId(UUID userId, Long serieTmdbId) {
        Map<String, List<Long>> result = new HashMap<>();
        for (WatchStatus status : WatchStatus.values()) {
            List<Long> ids = serieEpisodeWatchlistRepository.findEpisodeByUserIdAndSerieTmdbIdAndStatus(userId, serieTmdbId, status);
            result.put(status.name(), ids != null ? ids : Collections.emptyList());
        }
        return result;
    }
} 