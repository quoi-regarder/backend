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
import fr.quoi_regarder.mapper.serie.SerieEpisodeWatchlistMapper;
import fr.quoi_regarder.mapper.serie.SerieSeasonWatchlistMapper;
import fr.quoi_regarder.repository.serie.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SerieSeasonWatchlistService implements WatchlistService<SerieSeasonWatchlistDto> {
    private final SerieEpisodeWatchlistRepository serieEpisodeWatchlistRepository;
    private final SerieSeasonWatchlistRepository serieSeasonWatchlistRepository;
    private final SerieEpisodeWatchlistMapper serieEpisodeWatchlistMapper;
    private final SerieSeasonWatchlistMapper serieSeasonWatchlistMapper;
    private final SerieWatchlistEventService serieWatchlistEventService;
    private final WatchlistStatusCalculator watchlistStatusCalculator;
    private final SerieWatchlistRepository serieWatchlistRepository;
    private final SerieEpisodeRepository serieEpisodeRepository;
    private final SerieSeasonRepository serieSeasonRepository;

    @Override
    @Transactional
    public void handleAction(UUID userId, Long serieId, Long seasonId, EventAction action, WatchStatus watchStatus) {
        switch (action) {
            case ADD -> addToWatchlistInternal(userId, serieId, seasonId, watchStatus);
            case UPDATE -> updateStatusInternal(userId, serieId, seasonId, watchStatus);
            case REMOVE -> removeFromWatchlistInternal(userId, serieId, seasonId);
        }
        serieWatchlistEventService.publishWatchlistEvents(userId, serieId);
    }

    @Override
    public SerieContext getContext() {
        return SerieContext.SEASON;
    }

    private void removeFromWatchlistInternal(UUID userId, Long serieId, Long seasonId) {
        serieEpisodeWatchlistRepository.deleteByUserIdAndSeasonId(userId, seasonId);
        serieSeasonWatchlistRepository.deleteByUserIdAndSeasonId(userId, seasonId);

        List<SerieSeasonWatchlist> seasons = serieSeasonWatchlistRepository.findByIdUserIdAndSerieSeasonSerieTmdbId(userId, serieId);

        if (seasons.isEmpty()) {
            serieWatchlistRepository.deleteByUserIdAndTmdbId(userId, serieId);
        }
    }

    private void addToWatchlistInternal(UUID userId, Long serieId, Long seasonId, WatchStatus watchStatus) {
        List<SerieEpisode> episodes = serieEpisodeRepository.findBySeasonSeasonId(seasonId);

        if (episodes.isEmpty()) {
            throw new RuntimeException("Season data not fully loaded yet. Please try again in a few moments.");
        }

        SerieSeasonWatchlistDto seasonWatchlistDto = new SerieSeasonWatchlistDto();
        seasonWatchlistDto.setTmdbId(seasonId);
        seasonWatchlistDto.setUserId(userId);
        seasonWatchlistDto.setStatus(watchStatus);
        seasonWatchlistDto.setCreatedAt(new Date(System.currentTimeMillis()));

        serieSeasonWatchlistRepository.save(serieSeasonWatchlistMapper.toEntity(seasonWatchlistDto));

        List<SerieEpisodeWatchlist> episodeWatchlist = episodes.stream()
                .map(episode -> {
                    SerieEpisodeWatchlistDto episodeDto = new SerieEpisodeWatchlistDto();
                    episodeDto.setTmdbId(episode.getEpisodeId());
                    episodeDto.setUserId(userId);
                    episodeDto.setStatus(watchStatus);
                    episodeDto.setCreatedAt(new Date(System.currentTimeMillis()));
                    return serieEpisodeWatchlistMapper.toEntity(episodeDto);
                })
                .toList();

        serieEpisodeWatchlistRepository.saveAll(episodeWatchlist);

        updateSerieStatus(userId, serieId);
    }

    private void updateStatusInternal(UUID userId, Long serieId, Long seasonId, WatchStatus status) {
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