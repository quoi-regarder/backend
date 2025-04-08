package fr.quoi_regarder.service.movie.watchlist;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.dto.serie.SerieEpisodeWatchlistDto;
import fr.quoi_regarder.dto.serie.SerieSeasonWatchlistDto;
import fr.quoi_regarder.dto.serie.SerieWatchlistDto;
import fr.quoi_regarder.entity.serie.SerieEpisode;
import fr.quoi_regarder.entity.serie.SerieEpisodeWatchlist;
import fr.quoi_regarder.entity.serie.SerieSeason;
import fr.quoi_regarder.entity.serie.SerieSeasonWatchlist;
import fr.quoi_regarder.mapper.serie.SerieEpisodeWatchlistMapper;
import fr.quoi_regarder.mapper.serie.SerieSeasonWatchlistMapper;
import fr.quoi_regarder.mapper.serie.SerieWatchlistMapper;
import fr.quoi_regarder.repository.serie.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WatchlistStatusCalculator {
    private final SerieEpisodeWatchlistRepository episodeWatchlistRepository;
    private final SerieSeasonWatchlistRepository seasonWatchlistRepository;
    private final SerieWatchlistRepository serieWatchlistRepository;
    @Qualifier("serieEpisodeWatchlistMapper")
    private final SerieEpisodeWatchlistMapper episodeWatchlistMapper;
    @Qualifier("serieSeasonWatchlistMapper")
    private final SerieSeasonWatchlistMapper seasonWatchlistMapper;
    private final SerieWatchlistMapper serieWatchlistMapper;
    private final SerieEpisodeRepository episodeRepository;
    private final SerieSeasonRepository seasonRepository;

    public WatchStatus calculateStatus(List<?> watchlists, Long totalCount) {
        if (watchlists.size() == totalCount &&
                watchlists.stream().allMatch(watchlist -> getStatus(watchlist) == WatchStatus.watched)) {
            return WatchStatus.watched;
        } else if (watchlists.stream().anyMatch(watchlist ->
                getStatus(watchlist) == WatchStatus.watching || getStatus(watchlist) == WatchStatus.watched)) {
            return WatchStatus.watching;
        }
        return WatchStatus.to_watch;
    }

    private WatchStatus getStatus(Object watchlist) {
        if (watchlist instanceof SerieEpisodeWatchlist) {
            return ((SerieEpisodeWatchlist) watchlist).getStatus();
        } else if (watchlist instanceof SerieSeasonWatchlist) {
            return ((SerieSeasonWatchlist) watchlist).getStatus();
        }
        throw new IllegalArgumentException("Unsupported watchlist type");
    }

    public void updateOrCreateWatchlist(UUID userId, Long tmdbId, WatchStatus status, Object watchlistType) {
        switch (watchlistType) {
            case SerieEpisodeWatchlist serieEpisodeWatchlist -> updateOrCreateEpisodeWatchlist(userId, tmdbId, status);
            case SerieSeasonWatchlist serieSeasonWatchlist -> {
                updateOrCreateSeasonWatchlist(userId, tmdbId, status);
                if (status == WatchStatus.watched) {
                    propagateStatusToEpisodes(userId, tmdbId, status);
                }
            }
            case SerieWatchlistDto serieWatchlistDto -> {
                updateOrCreateSerieWatchlist(userId, tmdbId, status);
                if (status == WatchStatus.watched) {
                    propagateStatusToSeasonsAndEpisodes(userId, tmdbId, status);
                }
            }
            case null, default -> throw new IllegalArgumentException("Unsupported watchlist type");
        }
    }

    private void propagateStatusToSeasonsAndEpisodes(UUID userId, Long serieId, WatchStatus status) {
        List<SerieSeason> seasons = seasonRepository.findBySerieTmdbId(serieId);
        for (SerieSeason season : seasons) {
            if (seasonRepository.existsById(season.getSeasonId())) {
                updateOrCreateSeasonWatchlist(userId, season.getSeasonId(), status);
                propagateStatusToEpisodes(userId, season.getSeasonId(), status);
            }
        }
    }

    private void propagateStatusToEpisodes(UUID userId, Long seasonId, WatchStatus status) {
        List<SerieEpisode> episodes = episodeRepository.findBySeasonSeasonId(seasonId);
        for (SerieEpisode episode : episodes) {
            if (episodeRepository.existsById(episode.getEpisodeId())) {
                updateOrCreateEpisodeWatchlist(userId, episode.getEpisodeId(), status);
            }
        }
    }

    private void updateOrCreateEpisodeWatchlist(UUID userId, Long episodeId, WatchStatus status) {
        if (!episodeRepository.existsById(episodeId)) {
            return;
        }

        episodeWatchlistRepository.findByIdUserIdAndIdTmdbId(userId, episodeId)
                .ifPresentOrElse(
                        watchlist -> {
                            watchlist.setStatus(status);
                            episodeWatchlistRepository.save(watchlist);
                        },
                        () -> {
                            SerieEpisodeWatchlistDto dto = new SerieEpisodeWatchlistDto();
                            dto.setTmdbId(episodeId);
                            dto.setUserId(userId);
                            dto.setStatus(status);
                            dto.setCreatedAt(new Date(System.currentTimeMillis()));
                            episodeWatchlistRepository.save(episodeWatchlistMapper.toEntity(dto));
                        }
                );
    }

    private void updateOrCreateSeasonWatchlist(UUID userId, Long seasonId, WatchStatus status) {
        if (!seasonRepository.existsById(seasonId)) {
            return;
        }

        seasonWatchlistRepository.findByIdUserIdAndIdTmdbId(userId, seasonId)
                .ifPresentOrElse(
                        watchlist -> {
                            watchlist.setStatus(status);
                            seasonWatchlistRepository.save(watchlist);
                        },
                        () -> {
                            SerieSeasonWatchlistDto dto = new SerieSeasonWatchlistDto();
                            dto.setTmdbId(seasonId);
                            dto.setUserId(userId);
                            dto.setStatus(status);
                            dto.setCreatedAt(new Date(System.currentTimeMillis()));
                            seasonWatchlistRepository.save(seasonWatchlistMapper.toEntity(dto));
                        }
                );
    }

    private void updateOrCreateSerieWatchlist(UUID userId, Long serieId, WatchStatus status) {
        serieWatchlistRepository.findByIdUserIdAndIdTmdbId(userId, serieId)
                .ifPresentOrElse(
                        watchlist -> {
                            watchlist.setStatus(status);
                            serieWatchlistRepository.save(watchlist);
                        },
                        () -> {
                            SerieWatchlistDto dto = new SerieWatchlistDto();
                            dto.setTmdbId(serieId);
                            dto.setUserId(userId);
                            dto.setStatus(status);
                            dto.setCreatedAt(new Date(System.currentTimeMillis()));
                            serieWatchlistRepository.save(serieWatchlistMapper.toEntity(dto));
                        }
                );
    }
} 