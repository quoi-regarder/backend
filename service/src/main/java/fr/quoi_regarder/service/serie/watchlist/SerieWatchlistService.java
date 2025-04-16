package fr.quoi_regarder.service.serie.watchlist;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.dto.serie.SerieDto;
import fr.quoi_regarder.entity.serie.*;
import fr.quoi_regarder.entity.serie.id.SerieEpisodeWatchlistId;
import fr.quoi_regarder.entity.serie.id.SerieSeasonWatchlistId;
import fr.quoi_regarder.entity.serie.id.SerieWatchlistId;
import fr.quoi_regarder.entity.user.User;
import fr.quoi_regarder.mapper.serie.SerieMapper;
import fr.quoi_regarder.repository.serie.*;
import fr.quoi_regarder.repository.user.UserRepository;
import fr.quoi_regarder.service.serie.SerieService;
import fr.quoi_regarder.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SerieWatchlistService {
    private final SerieEpisodeWatchlistRepository serieEpisodeWatchlistRepository;
    private final SerieSeasonWatchlistRepository serieSeasonWatchlistRepository;
    private final SerieWatchlistEventService serieWatchlistEventService;
    private final SerieWatchlistRepository serieWatchlistRepository;
    private final SerieEpisodeRepository serieEpisodeRepository;
    private final SerieSeasonRepository serieSeasonRepository;
    private final SerieRepository serieRepository;
    private final UserRepository userRepository;
    private final SerieService serieService;
    private final UserService userService;
    private final SerieMapper serieMapper;

    public Page<SerieDto> findSeriesByUserIdAndStatus(UUID userId, WatchStatus status, int page, int limit) {
        String userLanguage = userService.getCurrentUserLanguage();

        return serieRepository.findByWatchlistUserIdAndWatchlistStatus(userId, status, PageRequest.of(page, limit))
                .map(serie -> serieMapper.toDto(serie, userLanguage));
    }

    public Map<String, Long> calculateTotalRuntimeForUser(UUID userId) {
        return serieWatchlistEventService.getTotalRuntimeForUser(userId);
    }

    @Transactional
    public void addToWatchlist(UUID userId, Long serieId, WatchStatus status) {
        serieService.loadSerie(userId, serieId, (uid, sid) -> {
            addToWatchlistInternal(uid, sid, status);

            serieWatchlistEventService.publishWatchlistEvents(userId, serieId);
        });
    }

    @Transactional
    public void updateWatchStatus(UUID userId, Long serieId, WatchStatus status) {
        serieService.loadSerie(userId, serieId, (uid, sid) -> {
            addToWatchlistInternal(uid, sid, status);

            serieWatchlistEventService.publishWatchlistEvents(userId, serieId);

        });
    }

    @Transactional
    public void removeFromWatchlist(UUID userId, Long serieId) {
        serieService.loadSerie(userId, serieId, (uid, sid) -> {
            removeFromWatchlistInternal(uid, sid);

            serieWatchlistEventService.publishWatchlistEvents(userId, serieId);
        });
    }

    private void addToWatchlistInternal(UUID userId, Long serieId, WatchStatus watchStatus) {
        // Get all seasons and episodes in one query
        List<SerieSeason> seasons = serieSeasonRepository.findBySerieTmdbId(serieId);
        List<SerieEpisode> episodes = serieEpisodeRepository.findBySerieTmdbId(serieId);

        if (seasons.isEmpty() || episodes.isEmpty()) {
            throw new RuntimeException("Series data not fully loaded yet. Please try again in a few moments.");
        }

        // Get serie and user
        Serie serie = serieRepository.findByTmdbId(serieId)
                .orElseThrow(() -> new RuntimeException("Serie not found"));
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create and save serie watchlist
        SerieWatchlist serieWatchlist = new SerieWatchlist();
        SerieWatchlistId serieWatchlistId = new SerieWatchlistId();
        serieWatchlistId.setTmdbId(serieId);
        serieWatchlistId.setUserId(userId);
        serieWatchlist.setId(serieWatchlistId);
        serieWatchlist.setSerie(serie);
        serieWatchlist.setUser(user);
        serieWatchlist.setStatus(watchStatus);
        serieWatchlistRepository.save(serieWatchlist);

        // Create and save season watchlist
        List<SerieSeasonWatchlist> seasonWatchlists = seasons.stream()
                .map(season -> {
                    SerieSeasonWatchlist seasonWatchlist = new SerieSeasonWatchlist();
                    SerieSeasonWatchlistId seasonWatchlistId = new SerieSeasonWatchlistId();
                    seasonWatchlistId.setTmdbId(season.getSeasonId());
                    seasonWatchlistId.setUserId(userId);
                    seasonWatchlist.setId(seasonWatchlistId);
                    seasonWatchlist.setSerieSeason(season);
                    seasonWatchlist.setUser(user);
                    seasonWatchlist.setStatus(watchStatus);
                    return seasonWatchlist;
                })
                .toList();
        serieSeasonWatchlistRepository.saveAll(seasonWatchlists);

        // Create and save episode watchlist
        List<SerieEpisodeWatchlist> episodeWatchlists = episodes.stream()
                .map(episode -> {
                    SerieEpisodeWatchlist episodeWatchlist = new SerieEpisodeWatchlist();
                    SerieEpisodeWatchlistId episodeWatchlistId = new SerieEpisodeWatchlistId();
                    episodeWatchlistId.setTmdbId(episode.getEpisodeId());
                    episodeWatchlistId.setUserId(userId);
                    episodeWatchlist.setId(episodeWatchlistId);
                    episodeWatchlist.setSerieEpisode(episode);
                    episodeWatchlist.setUser(user);
                    episodeWatchlist.setStatus(watchStatus);
                    return episodeWatchlist;
                })
                .toList();
        serieEpisodeWatchlistRepository.saveAll(episodeWatchlists);
    }

    private void removeFromWatchlistInternal(UUID userId, Long serieId) {
        serieWatchlistRepository.deleteByUserIdAndTmdbId(userId, serieId);
        serieSeasonWatchlistRepository.deleteByUserIdAndTmdbId(userId, serieId);
        serieEpisodeWatchlistRepository.deleteByUserIdAndTmdbId(userId, serieId);
    }
}