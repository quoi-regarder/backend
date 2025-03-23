package fr.quoi_regarder.service.serie;

import fr.quoi_regarder.commons.enums.EventAction;
import fr.quoi_regarder.commons.enums.SerieContext;
import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.dto.serie.SerieEpisodeWatchlistDto;
import fr.quoi_regarder.dto.serie.SerieSeasonWatchlistDto;
import fr.quoi_regarder.dto.serie.SerieWatchlistDto;
import fr.quoi_regarder.entity.serie.SerieEpisode;
import fr.quoi_regarder.entity.serie.SerieEpisodeWatchlist;
import fr.quoi_regarder.entity.serie.SerieSeasonWatchlist;
import fr.quoi_regarder.event.serie.SerieDataLoadedEvent;
import fr.quoi_regarder.mapper.serie.SerieEpisodeWatchlistMapper;
import fr.quoi_regarder.mapper.serie.SerieSeasonWatchlistMapper;
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
public class SerieSeasonWatchlistService implements WatchlistService<SerieSeasonWatchlistDto, SerieDataLoadedEvent.SeasonEvent> {
    private final SerieEpisodeWatchlistRepository serieEpisodeWatchlistRepository;
    private final SerieSeasonWatchlistRepository serieSeasonWatchlistRepository;
    private final SerieEpisodeWatchlistMapper serieEpisodeWatchlistMapper;
    private final SerieSeasonWatchlistMapper serieSeasonWatchlistMapper;
    private final SerieWatchlistEventService serieWatchlistEventService;
    private final WatchlistStatusCalculator watchlistStatusCalculator;
    private final SerieWatchlistRepository serieWatchlistRepository;
    private final SerieEpisodeRepository serieEpisodeRepository;
    private final SerieSeasonRepository serieSeasonRepository;
    private final SerieService serieService;

    @Override
    public void addToWatchlist(UUID userId, Long serieId, SerieSeasonWatchlistDto dto) {
        serieService.ensureSerieExists(userId, serieId, dto.getTmdbId(), getContext(), EventAction.ADD, dto.getStatus());
    }

    @Override
    public void updateStatus(UUID userId, Long serieId, Long seasonId, WatchStatus status) {
        serieService.ensureSerieExists(userId, serieId, seasonId, getContext(), EventAction.UPDATE, status);
    }

    @Override
    public void removeFromWatchlist(UUID userId, Long serieId, Long seasonId) {
        serieService.ensureSerieExists(userId, serieId, seasonId, getContext(), EventAction.REMOVE, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDataLoadedEvent(SerieDataLoadedEvent.SeasonEvent event) {
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
        return SerieContext.SEASON;
    }

    private void removeSerieWatchlist(UUID userId, Long serieId, Long seasonId) {
        serieEpisodeWatchlistRepository.deleteByUserIdAndSeasonId(userId, seasonId);
        serieSeasonWatchlistRepository.deleteByUserIdAndSeasonId(userId, seasonId);

        List<SerieSeasonWatchlist> seasons = serieSeasonWatchlistRepository.findByIdUserIdAndSerieSeasonSerieTmdbId(userId, serieId);

        if (seasons.isEmpty()) {
            serieWatchlistRepository.deleteByUserIdAndTmdbId(userId, serieId);
        }
    }

    private void addSerieWatchlist(UUID userId, Long serieId, Long seasonId, WatchStatus status) {
        List<SerieEpisode> episodes = serieEpisodeRepository.findBySeasonSeasonId(seasonId);

        SerieSeasonWatchlistDto seasonWatchlistDto = new SerieSeasonWatchlistDto();
        seasonWatchlistDto.setTmdbId(seasonId);
        seasonWatchlistDto.setUserId(userId);
        seasonWatchlistDto.setStatus(status);

        serieSeasonWatchlistRepository.save(serieSeasonWatchlistMapper.toEntity(seasonWatchlistDto));

        List<SerieEpisodeWatchlist> episodeWatchlist = episodes.stream()
                .map(episode -> {
                    SerieEpisodeWatchlistDto dto = new SerieEpisodeWatchlistDto();
                    dto.setTmdbId(episode.getEpisodeId());
                    dto.setUserId(userId);
                    dto.setStatus(status);
                    return serieEpisodeWatchlistMapper.toEntity(dto);
                })
                .toList();

        serieEpisodeWatchlistRepository.saveAll(episodeWatchlist);

        updateSerieStatus(userId, serieId);
    }

    private void updateSerieWatchlist(UUID userId, Long serieId, Long seasonId, WatchStatus status) {
        List<SerieEpisodeWatchlist> episodeWatchlists = serieEpisodeWatchlistRepository
                .findByIdUserIdAndSerieEpisodeSeasonSeasonId(userId, seasonId);

        watchlistStatusCalculator.updateOrCreateWatchlist(userId, seasonId, status, new SerieSeasonWatchlist());

        episodeWatchlists.forEach(watchlist -> watchlist.setStatus(status));
        serieEpisodeWatchlistRepository.saveAll(episodeWatchlists);

        updateSerieStatus(userId, serieId);
    }

    private void updateSerieStatus(UUID userId, Long serieId) {
        List<SerieSeasonWatchlist> seasonWatchlists = serieSeasonWatchlistRepository
                .findSeasonByUserIdAndSerieTmdbId(userId, serieId);
        Long totalSeasonCount = serieSeasonRepository.countBySerieTmdbId(serieId);

        if (seasonWatchlists.isEmpty()) {
            serieWatchlistRepository.deleteByUserIdAndTmdbId(userId, serieId);
            return;
        }

        WatchStatus serieStatus = watchlistStatusCalculator.calculateStatus(seasonWatchlists, totalSeasonCount);
        watchlistStatusCalculator.updateOrCreateWatchlist(userId, serieId, serieStatus, new SerieWatchlistDto());
    }
}