package fr.quoi_regarder.service.serie.watchlist;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.dto.serie.SerieWatchlistDto;
import fr.quoi_regarder.entity.serie.SerieEpisode;
import fr.quoi_regarder.entity.serie.SerieEpisodeWatchlist;
import fr.quoi_regarder.entity.serie.SerieSeason;
import fr.quoi_regarder.entity.serie.SerieSeasonWatchlist;
import fr.quoi_regarder.entity.serie.id.SerieEpisodeWatchlistId;
import fr.quoi_regarder.entity.user.User;
import fr.quoi_regarder.repository.serie.*;
import fr.quoi_regarder.repository.user.UserRepository;
import fr.quoi_regarder.service.movie.watchlist.WatchlistStatusCalculator;
import fr.quoi_regarder.service.serie.SerieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SerieEpisodeWatchlistService {
    private final SerieEpisodeWatchlistRepository serieEpisodeWatchlistRepository;
    private final SerieSeasonWatchlistRepository serieSeasonWatchlistRepository;
    private final SerieWatchlistEventService serieWatchlistEventService;
    private final WatchlistStatusCalculator watchlistStatusCalculator;
    private final SerieWatchlistRepository serieWatchlistRepository;
    private final SerieEpisodeRepository serieEpisodeRepository;
    private final SerieSeasonRepository serieSeasonRepository;
    private final UserRepository userRepository;
    private final SerieService serieService;

    @Transactional
    public void addToWatchlist(UUID userId, Long serieId, Long episodeId, WatchStatus status) {
        serieService.loadSerie(userId, serieId, (uid, sid) -> {
            addToWatchlistInternal(uid, sid, episodeId, status);

            serieWatchlistEventService.publishWatchlistEvents(userId, serieId);
        });
    }

    @Transactional
    public void updateWatchStatus(UUID userId, Long serieId, Long episodeId, WatchStatus status) {
        serieService.loadSerie(userId, serieId, (uid, sid) -> {
            updateStatusInternal(uid, sid, episodeId, status);

            serieWatchlistEventService.publishWatchlistEvents(userId, serieId);
        });
    }

    @Transactional
    public void removeFromWatchlist(UUID userId, Long serieId, Long episodeId) {
        serieService.loadSerie(userId, serieId, (uid, sid) -> {
            removeFromWatchlistInternal(uid, sid, episodeId);

            serieWatchlistEventService.publishWatchlistEvents(userId, serieId);
        });
    }

    private void removeFromWatchlistInternal(UUID userId, Long serieId, Long episodeId) {
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

    private void addToWatchlistInternal(UUID userId, Long serieId, Long episodeId, WatchStatus watchStatus) {
        SerieEpisode episode = serieEpisodeRepository.findById(episodeId)
                .orElseThrow(() -> new RuntimeException("Episode not found"));
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SerieEpisodeWatchlist episodeWatchlist = new SerieEpisodeWatchlist();
        SerieEpisodeWatchlistId episodeWatchlistId = new SerieEpisodeWatchlistId();
        episodeWatchlistId.setTmdbId(episodeId);
        episodeWatchlistId.setUserId(userId);
        episodeWatchlist.setId(episodeWatchlistId);
        episodeWatchlist.setSerieEpisode(episode);
        episodeWatchlist.setUser(user);
        episodeWatchlist.setStatus(watchStatus);
        episodeWatchlist.setCreatedAt(new Date(System.currentTimeMillis()));

        serieEpisodeWatchlistRepository.save(episodeWatchlist);
        updateSerieStatus(userId, serieId, episodeId);
    }

    private void updateStatusInternal(UUID userId, Long serieId, Long episodeId, WatchStatus status) {
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

        if (seasonWatchlists.isEmpty()) {
            serieWatchlistRepository.deleteByUserIdAndTmdbId(userId, serieId);
            return;
        }

        WatchStatus serieStatus = watchlistStatusCalculator.calculateStatus(seasonWatchlists, totalSeasonCount);
        watchlistStatusCalculator.updateOrCreateWatchlist(userId, serieId, serieStatus, new SerieWatchlistDto());
    }
}
