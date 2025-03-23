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
import fr.quoi_regarder.event.serie.SerieDataLoadedEvent;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SerieWatchlistService implements WatchlistService<SerieWatchlistDto, SerieDataLoadedEvent.SerieEvent> {
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

    @Override
    @Transactional
    public void addToWatchlist(UUID userId, Long serieId, SerieWatchlistDto dto) {
        serieService.ensureSerieExists(userId, serieId, null, getContext(), EventAction.ADD, dto.getStatus());
    }

    @Override
    @Transactional
    public void updateStatus(UUID userId, Long serieId, Long contextId, WatchStatus status) {
        serieService.ensureSerieExists(userId, serieId, null, getContext(), EventAction.UPDATE, status);
    }

    @Override
    @Transactional
    public void removeFromWatchlist(UUID userId, Long serieId, Long contextId) {
        serieService.ensureSerieExists(userId, serieId, null, getContext(), EventAction.REMOVE, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDataLoadedEvent(SerieDataLoadedEvent.SerieEvent event) {
        switch (event.getAction()) {
            case ADD -> addSerieWatchlist(event.getUserId(), event.getSerieId(), event.getWatchStatus());
            case UPDATE -> updateSerieWatchlist(event.getUserId(), event.getSerieId(), event.getWatchStatus());
            case REMOVE -> removeSerieWatchlist(event.getUserId(), event.getSerieId());
        }

        serieWatchlistEventService.publishWatchlistEvents(event.getUserId(), event.getSerieId());
    }

    @Override
    public SerieContext getContext() {
        return SerieContext.SERIE;
    }


    private void removeSerieWatchlist(UUID userId, Long serieId) {
        serieWatchlistRepository.deleteByUserIdAndTmdbId(userId, serieId);
        serieSeasonWatchlistRepository.deleteByUserIdAndTmdbId(userId, serieId);
        serieEpisodeWatchlistRepository.deleteByUserIdAndTmdbId(userId, serieId);
    }

    private void addSerieWatchlist(UUID userId, Long serieId, WatchStatus watchStatus) {
        // Get all seasons and episodes in one query
        List<SerieSeason> seasons = serieSeasonRepository.findBySerieTmdbId(serieId);
        List<SerieEpisode> episodes = serieEpisodeRepository.findBySerieTmdbId(serieId);

        // Create and save serie watchlist
        SerieWatchlistDto serieWatchlistDto = new SerieWatchlistDto();
        serieWatchlistDto.setTmdbId(serieId);
        serieWatchlistDto.setUserId(userId);
        serieWatchlistDto.setStatus(watchStatus);
        serieWatchlistRepository.save(serieWatchlistMapper.toEntity(serieWatchlistDto));

        // Create and save season watchlist
        List<SerieSeasonWatchlist> seasonWatchlists = seasons.stream()
                .map(season -> {
                    SerieSeasonWatchlistDto dto = new SerieSeasonWatchlistDto();
                    dto.setTmdbId(season.getSeasonId());
                    dto.setUserId(userId);
                    dto.setStatus(watchStatus);
                    return serieSeasonWatchlistMapper.toEntity(dto);
                })
                .toList();
        serieSeasonWatchlistRepository.saveAll(seasonWatchlists);

        // Create and save episode watchlist
        List<SerieEpisodeWatchlist> episodeWatchlists = episodes.stream()
                .map(episode -> {
                    SerieEpisodeWatchlistDto dto = new SerieEpisodeWatchlistDto();
                    dto.setTmdbId(episode.getEpisodeId());
                    dto.setUserId(userId);
                    dto.setStatus(watchStatus);
                    return serieEpisodeWatchlistMapper.toEntity(dto);
                })
                .toList();
        serieEpisodeWatchlistRepository.saveAll(episodeWatchlists);
    }

    private void updateSerieWatchlist(UUID userId, Long serieId, WatchStatus watchStatus) {
        // Update serie watchlist
        SerieWatchlistId serieWatchlistId = new SerieWatchlistId();
        serieWatchlistId.setTmdbId(serieId);
        serieWatchlistId.setUserId(userId);
        serieWatchlistRepository.findById(serieWatchlistId)
                .ifPresent(serieWatchlist -> {
                    SerieWatchlistDto serieWatchlistDto = serieWatchlistMapper.toDto(serieWatchlist);
                    serieWatchlistDto.setStatus(watchStatus);
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

        // Find all watchlists in batch
        Map<SerieSeasonWatchlistId, SerieSeasonWatchlist> seasonWatchlists = serieSeasonWatchlistRepository.findAllById(seasonWatchlistIds)
                .stream()
                .collect(Collectors.toMap(SerieSeasonWatchlist::getId, Function.identity()));

        Map<SerieEpisodeWatchlistId, SerieEpisodeWatchlist> episodeWatchlists = serieEpisodeWatchlistRepository.findAllById(episodeWatchlistIds)
                .stream()
                .collect(Collectors.toMap(SerieEpisodeWatchlist::getId, Function.identity()));

        // Update season watchlists in batch
        List<SerieSeasonWatchlist> updatedSeasonWatchlists = seasonWatchlists.values().stream()
                .map(seasonWatchlist -> {
                    SerieSeasonWatchlistDto dto = serieSeasonWatchlistMapper.toDto(seasonWatchlist);
                    dto.setStatus(watchStatus);
                    serieSeasonWatchlistMapper.partialUpdate(dto, seasonWatchlist);
                    return seasonWatchlist;
                })
                .toList();
        serieSeasonWatchlistRepository.saveAll(updatedSeasonWatchlists);

        // Update episode watchlists in batch
        List<SerieEpisodeWatchlist> updatedEpisodeWatchlists = episodeWatchlists.values().stream()
                .map(episodeWatchlist -> {
                    SerieEpisodeWatchlistDto dto = serieEpisodeWatchlistMapper.toDto(episodeWatchlist);
                    dto.setStatus(watchStatus);
                    serieEpisodeWatchlistMapper.partialUpdate(dto, episodeWatchlist);
                    return episodeWatchlist;
                })
                .toList();
        serieEpisodeWatchlistRepository.saveAll(updatedEpisodeWatchlists);
    }
}