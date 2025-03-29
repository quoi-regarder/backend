package fr.quoi_regarder.service.serie;

import fr.quoi_regarder.commons.enums.EventAction;
import fr.quoi_regarder.commons.enums.SerieContext;
import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.dto.serie.SerieDto;
import fr.quoi_regarder.dto.serie.SerieEpisodeWatchlistDto;
import fr.quoi_regarder.dto.serie.SerieSeasonWatchlistDto;
import fr.quoi_regarder.dto.serie.SerieWatchlistDto;
import fr.quoi_regarder.entity.serie.SerieEpisode;
import fr.quoi_regarder.entity.serie.SerieEpisodeWatchlist;
import fr.quoi_regarder.entity.serie.SerieSeason;
import fr.quoi_regarder.entity.serie.SerieSeasonWatchlist;
import fr.quoi_regarder.entity.serie.id.SerieEpisodeWatchlistId;
import fr.quoi_regarder.entity.serie.id.SerieSeasonWatchlistId;
import fr.quoi_regarder.entity.serie.id.SerieWatchlistId;
import fr.quoi_regarder.mapper.serie.SerieEpisodeWatchlistMapper;
import fr.quoi_regarder.mapper.serie.SerieMapper;
import fr.quoi_regarder.mapper.serie.SerieSeasonWatchlistMapper;
import fr.quoi_regarder.mapper.serie.SerieWatchlistMapper;
import fr.quoi_regarder.repository.serie.*;
import fr.quoi_regarder.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SerieWatchlistService implements WatchlistService<SerieWatchlistDto> {
    private final SerieEpisodeWatchlistRepository serieEpisodeWatchlistRepository;
    private final SerieSeasonWatchlistRepository serieSeasonWatchlistRepository;
    private final SerieEpisodeWatchlistMapper serieEpisodeWatchlistMapper;
    private final SerieSeasonWatchlistMapper serieSeasonWatchlistMapper;
    private final SerieWatchlistEventService serieWatchlistEventService;
    private final SerieWatchlistRepository serieWatchlistRepository;
    private final SerieEpisodeRepository serieEpisodeRepository;
    private final SerieSeasonRepository serieSeasonRepository;
    private final SerieWatchlistMapper serieWatchlistMapper;
    private final SerieRepository serieRepository;
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

    @Override
    @Transactional
    public void handleAction(UUID userId, Long serieId, Long contextId, EventAction action, WatchStatus watchStatus) {
        switch (action) {
            case ADD -> addToWatchlistInternal(userId, serieId, watchStatus);
            case UPDATE -> updateStatusInternal(userId, serieId, watchStatus);
            case REMOVE -> removeFromWatchlistInternal(userId, serieId);
        }
        serieWatchlistEventService.publishWatchlistEvents(userId, serieId);
    }

    @Override
    public SerieContext getContext() {
        return SerieContext.SERIE;
    }

    private void addToWatchlistInternal(UUID userId, Long serieId, WatchStatus watchStatus) {
        // Get all seasons and episodes in one query
        List<SerieSeason> seasons = serieSeasonRepository.findBySerieTmdbId(serieId);
        List<SerieEpisode> episodes = serieEpisodeRepository.findBySerieTmdbId(serieId);

        if (seasons.isEmpty() || episodes.isEmpty()) {
            throw new RuntimeException("Series data not fully loaded yet. Please try again in a few moments.");
        }

        // Create and save serie watchlist
        SerieWatchlistDto serieWatchlistDto = new SerieWatchlistDto();
        serieWatchlistDto.setTmdbId(serieId);
        serieWatchlistDto.setUserId(userId);
        serieWatchlistDto.setStatus(watchStatus);
        serieWatchlistDto.setCreatedAt(new Date(System.currentTimeMillis()));
        serieWatchlistRepository.save(serieWatchlistMapper.toEntity(serieWatchlistDto));

        // Create and save season watchlist
        List<SerieSeasonWatchlist> seasonWatchlists = seasons.stream()
                .map(season -> {
                    SerieSeasonWatchlistDto seasonDto = new SerieSeasonWatchlistDto();
                    seasonDto.setTmdbId(season.getSeasonId());
                    seasonDto.setUserId(userId);
                    seasonDto.setStatus(watchStatus);
                    seasonDto.setCreatedAt(new Date(System.currentTimeMillis()));
                    return serieSeasonWatchlistMapper.toEntity(seasonDto);
                })
                .toList();
        serieSeasonWatchlistRepository.saveAll(seasonWatchlists);

        // Create and save episode watchlist
        List<SerieEpisodeWatchlist> episodeWatchlists = episodes.stream()
                .map(episode -> {
                    SerieEpisodeWatchlistDto episodeDto = new SerieEpisodeWatchlistDto();
                    episodeDto.setTmdbId(episode.getEpisodeId());
                    episodeDto.setUserId(userId);
                    episodeDto.setStatus(watchStatus);
                    episodeDto.setCreatedAt(new Date(System.currentTimeMillis()));
                    return serieEpisodeWatchlistMapper.toEntity(episodeDto);
                })
                .toList();
        serieEpisodeWatchlistRepository.saveAll(episodeWatchlists);
    }

    private void updateStatusInternal(UUID userId, Long serieId, WatchStatus status) {
        // Update serie watchlist
        SerieWatchlistId serieWatchlistId = new SerieWatchlistId();
        serieWatchlistId.setTmdbId(serieId);
        serieWatchlistId.setUserId(userId);
        serieWatchlistRepository.findById(serieWatchlistId)
                .ifPresent(serieWatchlist -> {
                    SerieWatchlistDto serieWatchlistDto = serieWatchlistMapper.toDto(serieWatchlist);
                    serieWatchlistDto.setStatus(status);
                    serieWatchlistMapper.partialUpdate(serieWatchlistDto, serieWatchlist);
                    serieWatchlistRepository.save(serieWatchlist);
                });

        // Get all seasons and episodes in one query
        List<SerieSeason> seasons = serieSeasonRepository.findBySerieTmdbId(serieId);
        List<SerieEpisode> episodes = serieEpisodeRepository.findBySerieTmdbId(serieId);

        // Create IDs for batch lookup
        List<SerieSeasonWatchlistId> seasonWatchlistIds = seasons.stream()
                .map(season -> {
                    SerieSeasonWatchlistId id = new SerieSeasonWatchlistId();
                    id.setTmdbId(season.getSeasonId());
                    id.setUserId(userId);
                    return id;
                })
                .toList();

        List<SerieEpisodeWatchlistId> episodeWatchlistIds = episodes.stream()
                .map(episode -> {
                    SerieEpisodeWatchlistId id = new SerieEpisodeWatchlistId();
                    id.setTmdbId(episode.getEpisodeId());
                    id.setUserId(userId);
                    return id;
                })
                .toList();

        // Find all existing watchlists in batch
        Map<SerieSeasonWatchlistId, SerieSeasonWatchlist> existingSeasonWatchlists = serieSeasonWatchlistRepository.findAllById(seasonWatchlistIds)
                .stream()
                .collect(Collectors.toMap(SerieSeasonWatchlist::getId, Function.identity()));

        Map<SerieEpisodeWatchlistId, SerieEpisodeWatchlist> existingEpisodeWatchlists = serieEpisodeWatchlistRepository.findAllById(episodeWatchlistIds)
                .stream()
                .collect(Collectors.toMap(SerieEpisodeWatchlist::getId, Function.identity()));

        // Create or update season watchlists
        List<SerieSeasonWatchlist> seasonWatchlists = seasonWatchlistIds.stream()
                .map(id -> {
                    SerieSeasonWatchlist watchlist = existingSeasonWatchlists.getOrDefault(id, new SerieSeasonWatchlist());
                    watchlist.setId(id);
                    SerieSeasonWatchlistDto dto = serieSeasonWatchlistMapper.toDto(watchlist);
                    dto.setStatus(status);
                    dto.setCreatedAt(new Date(System.currentTimeMillis()));
                    serieSeasonWatchlistMapper.partialUpdate(dto, watchlist);
                    return watchlist;
                })
                .toList();
        serieSeasonWatchlistRepository.saveAll(seasonWatchlists);

        // Create or update episode watchlists
        List<SerieEpisodeWatchlist> episodeWatchlists = episodeWatchlistIds.stream()
                .map(id -> {
                    SerieEpisodeWatchlist watchlist = existingEpisodeWatchlists.getOrDefault(id, new SerieEpisodeWatchlist());
                    watchlist.setId(id);
                    SerieEpisodeWatchlistDto dto = serieEpisodeWatchlistMapper.toDto(watchlist);
                    dto.setStatus(status);
                    dto.setCreatedAt(new Date(System.currentTimeMillis()));
                    serieEpisodeWatchlistMapper.partialUpdate(dto, watchlist);
                    return watchlist;
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