package fr.quoi_regarder.service.serie;

import fr.quoi_regarder.commons.enums.EventAction;
import fr.quoi_regarder.commons.enums.SerieContext;
import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.dto.serie.SerieEpisodeWatchlistDto;
import fr.quoi_regarder.dto.serie.SerieWatchlistDto;
import fr.quoi_regarder.entity.serie.SerieEpisodeWatchlist;
import fr.quoi_regarder.entity.serie.SerieSeason;
import fr.quoi_regarder.entity.serie.SerieSeasonWatchlist;
import fr.quoi_regarder.event.serie.SerieDataLoadedEvent;
import fr.quoi_regarder.mapper.serie.SerieEpisodeWatchlistMapper;
import fr.quoi_regarder.repository.serie.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SerieEpisodeWatchlistService implements WatchlistService<SerieEpisodeWatchlistDto, SerieDataLoadedEvent.EpisodeEvent> {
    private final SerieEpisodeWatchlistRepository serieEpisodeWatchlistRepository;
    private final SerieSeasonWatchlistRepository serieSeasonWatchlistRepository;
    private final SerieEpisodeWatchlistMapper serieEpisodeWatchlistMapper;
    private final SerieWatchlistEventService serieWatchlistEventService;
    private final WatchlistStatusCalculator watchlistStatusCalculator;
    private final SerieWatchlistRepository serieWatchlistRepository;
    private final SerieEpisodeRepository serieEpisodeRepository;
    private final SerieSeasonRepository serieSeasonRepository;
    private final SerieService serieService;

    @Override
    public void addToWatchlist(UUID userId, Long serieId, SerieEpisodeWatchlistDto dto) {
        serieService.ensureSerieExists(userId, serieId, dto.getTmdbId(), getContext(), EventAction.ADD, dto.getStatus());
    }

    @Override
    public void updateStatus(UUID userId, Long serieId, Long episodeId, WatchStatus status) {
        serieService.ensureSerieExists(userId, serieId, episodeId, getContext(), EventAction.UPDATE, status);
    }

    @Override
    public void removeFromWatchlist(UUID userId, Long serieId, Long episodeId) {
        serieService.ensureSerieExists(userId, serieId, episodeId, getContext(), EventAction.REMOVE, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDataLoadedEvent(SerieDataLoadedEvent.EpisodeEvent event) {
        switch (event.getAction()) {
            case ADD ->
                    addSerieWatchlist(event.getUserId(), event.getSerieId(), event.getContextId(), event.getWatchStatus());
            case UPDATE ->
                    updateSerieWatchlist(event.getUserId(), event.getSerieId(), event.getContextId(), event.getWatchStatus());
            case REMOVE -> removeSerieWatchlist(event.getUserId(), event.getSerieId(), event.getContextId());
        }

        serieWatchlistEventService.publishWatchlistEvents(event.getUserId(), event.getSerieId());
    }

    @Override
    public SerieContext getContext() {
        return SerieContext.EPISODE;
    }

    private void removeSerieWatchlist(UUID userId, Long serieId, Long episodeId) {
        serieEpisodeWatchlistRepository.deleteByUserIdAndEpisodeId(userId, episodeId);

        SerieSeason season = serieSeasonRepository.findByEpisodesEpisodeId(episodeId).orElse(null);

        if (season == null) {
            return;
        }

        serieSeasonWatchlistRepository.deleteByUserIdAndSeasonId(userId, season.getSeasonId());

        List<SerieSeasonWatchlist> seasons = serieSeasonWatchlistRepository.findByIdUserIdAndSerieSeasonSerieTmdbId(userId, serieId);

        if (seasons.isEmpty()) {
            serieWatchlistRepository.deleteByUserIdAndTmdbId(userId, serieId);
        }
    }

    private void addSerieWatchlist(UUID userId, Long serieId, Long episodeId, WatchStatus status) {
        SerieEpisodeWatchlistDto episodeWatchlistDto = new SerieEpisodeWatchlistDto();
        episodeWatchlistDto.setTmdbId(episodeId);
        episodeWatchlistDto.setUserId(userId);
        episodeWatchlistDto.setStatus(status);

        serieEpisodeWatchlistRepository.save(serieEpisodeWatchlistMapper.toEntity(episodeWatchlistDto));
        updateSerieStatus(userId, serieId, episodeId);
    }

    private void updateSerieWatchlist(UUID userId, Long serieId, Long episodeId, WatchStatus status) {
        watchlistStatusCalculator.updateOrCreateWatchlist(userId, episodeId, status, new SerieEpisodeWatchlist());
        updateSerieStatus(userId, serieId, episodeId);
    }

    private void updateSerieStatus(UUID userId, Long serieId, Long episodeId) {
        SerieSeason season = serieSeasonRepository.findByEpisodesEpisodeId(episodeId)
                .orElse(null);

        if (season == null) {
            return;
        }

        List<SerieEpisodeWatchlist> episodeWatchlists = serieEpisodeWatchlistRepository
                .findByIdUserIdAndSerieEpisodeSeasonSeasonId(userId, season.getSeasonId());
        Long totalEpisodeCount = serieEpisodeRepository.countBySeasonSeasonId(season.getSeasonId());

        WatchStatus seasonStatus = watchlistStatusCalculator.calculateStatus(episodeWatchlists, totalEpisodeCount);
        watchlistStatusCalculator.updateOrCreateWatchlist(userId, season.getSeasonId(), seasonStatus, new SerieSeasonWatchlist());

        updateOrCreateSerieWatchlist(userId, serieId);
    }

    private void updateOrCreateSerieWatchlist(UUID userId, Long serieId) {
        List<SerieSeasonWatchlist> seasonWatchlists = serieSeasonWatchlistRepository
                .findSeasonByUserIdAndSerieTmdbId(userId, serieId);
        Long totalSeasonCount = serieSeasonRepository.countBySerieTmdbId(serieId);

        WatchStatus serieStatus = watchlistStatusCalculator.calculateStatus(seasonWatchlists, totalSeasonCount);
        watchlistStatusCalculator.updateOrCreateWatchlist(userId, serieId, serieStatus, new SerieWatchlistDto());
    }
}
