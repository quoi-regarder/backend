package fr.quoi_regarder.service.serie;

import fr.quoi_regarder.commons.enums.EventAction;
import fr.quoi_regarder.commons.enums.LanguageIsoType;
import fr.quoi_regarder.commons.enums.SerieContext;
import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.entity.serie.Serie;
import fr.quoi_regarder.entity.serie.SerieEpisode;
import fr.quoi_regarder.entity.serie.SerieSeason;
import fr.quoi_regarder.entity.serie.SerieTranslation;
import fr.quoi_regarder.entity.serie.id.SerieTranslationId;
import fr.quoi_regarder.event.EventPublisherService;
import fr.quoi_regarder.event.serie.SerieDataLoadedEvent;
import fr.quoi_regarder.exception.exceptions.EntityNotExistsException;
import fr.quoi_regarder.repository.serie.SerieEpisodeRepository;
import fr.quoi_regarder.repository.serie.SerieRepository;
import fr.quoi_regarder.repository.serie.SerieSeasonRepository;
import fr.quoi_regarder.repository.serie.SerieTranslationRepository;
import fr.quoi_regarder.service.TmdbService;
import fr.quoi_regarder.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SerieService {
    private final SerieTranslationRepository serieTranslationRepository;
    private final SerieEpisodeRepository serieEpisodeRepository;
    private final SerieSeasonRepository serieSeasonRepository;
    private final EventPublisherService eventPublisherService;
    private final SerieRepository serieRepository;
    private final TmdbService tmdbService;
    private final UserService userService;

    @Transactional
    public void ensureSerieExists(
            UUID userId,
            Long serieId,
            Long contextId,
            SerieContext context,
            EventAction action,
            WatchStatus watchStatus
    ) {
        String userLanguage = userService.getCurrentUserLanguage();
        Map<String, Object> serieDetails = tmdbService.fetchSerieDetails(serieId, userLanguage);

        if (serieDetails == null || serieDetails.isEmpty()) {
            throw new EntityNotExistsException(Serie.class, "Failed to fetch series details, TMDB ID: " + serieId);
        }

        saveOrUpdateSerie(serieId, serieDetails);
        saveSerieTranslation(serieId, userLanguage, serieDetails);

        if (serieRepository.existsByTmdbId(serieId)) {
            CompletableFuture<Void> episodesFuture = CompletableFuture.runAsync(() ->
                            processSerieEpisodes(serieId, serieDetails))
                    .exceptionally(ex -> {
                        log.error("Error processing episodes", ex);
                        return null;
                    });

            CompletableFuture<Void> translationsFuture = CompletableFuture.runAsync(() ->
                            processTranslations(serieId, userLanguage))
                    .exceptionally(ex -> {
                        log.error("Error processing translations", ex);
                        return null;
                    });

            CompletableFuture.allOf(episodesFuture, translationsFuture)
                    .thenRunAsync(() -> publishDataLoadedEvent(userId, serieId, contextId, context, action, watchStatus))
                    .exceptionally(ex -> {
                        log.error("Error publishing event", ex);
                        return null;
                    });

        } else {
            throw new EntityNotExistsException(Serie.class, "Failed to save series with TMDB ID: " + serieId);
        }
    }

    public void processSerieEpisodes(Long serieId, Map<String, Object> serieDetails) {
        Serie serie = serieRepository.findByTmdbId(serieId).orElse(null);
        if (serie == null) return;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> seasons = (List<Map<String, Object>>) serieDetails.get("seasons");
        if (seasons == null || seasons.isEmpty()) return;

        seasons.parallelStream().forEach(season -> processSingleSeason(serie, season));
    }

    public void processTranslations(Long serieId, String defaultLanguage) {
        Serie serie = serieRepository.findByTmdbId(serieId).orElse(null);
        if (serie == null) return;

        LanguageIsoType defaultLangType = LanguageIsoType.findByCode(defaultLanguage);

        Arrays.stream(LanguageIsoType.values())
                .parallel()
                .filter(lang -> !lang.equals(defaultLangType))
                .forEach(lang -> processTranslation(serieId, lang));
    }

    private void processTranslation(Long serieId, LanguageIsoType lang) {
        try {
            SerieTranslationId id = new SerieTranslationId();
            id.setTmdbId(serieId);
            id.setLanguage(lang.getCode());

            if (!serieTranslationRepository.existsById(id)) {
                Map<String, Object> details = tmdbService.fetchSerieDetails(serieId, lang.getCode());
                if (details != null && !details.isEmpty()) {
                    saveSerieTranslation(serieId, lang.getCode(), details);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void processSingleSeason(Serie serie, Map<String, Object> season) {
        Integer seasonNumber = (Integer) season.get("season_number");
        Map<String, Object> seasonDetails = tmdbService.fetchSeasonDetails(
                serie.getTmdbId(), seasonNumber);

        if (seasonDetails == null) return;

        SerieSeason serieSeason = processSeasonDetails(serie, season, seasonDetails);
        processEpisodes(serie, serieSeason, seasonDetails);
    }

    private SerieSeason processSeasonDetails(Serie serie, Map<String, Object> season, Map<String, Object> seasonDetails) {
        Integer seasonNumber = (Integer) season.get("season_number");
        SerieSeason serieSeason = serieSeasonRepository.findBySerieAndSeasonNumber(serie, seasonNumber);

        if (serieSeason == null) {
            return createNewSeason(serie, season, seasonDetails);
        } else {
            return updateExistingSeason(serieSeason, season, seasonDetails);
        }
    }

    private SerieSeason createNewSeason(Serie serie, Map<String, Object> season, Map<String, Object> seasonDetails) {
        SerieSeason serieSeason = new SerieSeason();
        serieSeason.setSerie(serie);
        serieSeason.setSeasonNumber((Integer) season.get("season_number"));
        serieSeason.setEpisodeCount((Integer) season.get("episode_count"));
        serieSeason.setSeasonId(Long.valueOf(seasonDetails.get("id").toString()));

        String airDate = (String) seasonDetails.get("air_date");
        if (StringUtils.hasText(airDate)) {
            try {
                serieSeason.setAirDate(Date.valueOf(airDate));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return serieSeasonRepository.save(serieSeason);
    }

    private SerieSeason updateExistingSeason(SerieSeason serieSeason, Map<String, Object> season, Map<String, Object> seasonDetails) {
        boolean updated = false;

        Integer episodeCount = (Integer) season.get("episode_count");
        if (episodeCount != null && !episodeCount.equals(serieSeason.getEpisodeCount())) {
            serieSeason.setEpisodeCount(episodeCount);
            updated = true;
        }

        String airDate = (String) seasonDetails.get("air_date");
        if (StringUtils.hasText(airDate)) {
            try {
                serieSeason.setAirDate(Date.valueOf(airDate));
                updated = true;
            } catch (IllegalArgumentException ignored) {
            }
        } else if (serieSeason.getAirDate() != null) {
            serieSeason.setAirDate(null);
            updated = true;
        }

        return updated ? serieSeasonRepository.save(serieSeason) : serieSeason;
    }

    private void processEpisodes(Serie serie, SerieSeason serieSeason, Map<String, Object> seasonDetails) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> episodes = (List<Map<String, Object>>) seasonDetails.get("episodes");
        if (episodes == null || episodes.isEmpty()) return;

        List<Long> episodeIds = episodes.stream()
                .map(ep -> Long.valueOf(ep.get("id").toString()))
                .collect(Collectors.toList());

        Set<Long> existingIds = serieEpisodeRepository.findAllById(episodeIds).stream()
                .map(SerieEpisode::getEpisodeId)
                .collect(Collectors.toSet());

        List<SerieEpisode> episodesToSave = episodes.parallelStream()
                .map(episode -> {
                    Long episodeId = Long.valueOf(episode.get("id").toString());
                    return existingIds.contains(episodeId) ? null : createEpisodeEntity(serie, serieSeason, episode);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!episodesToSave.isEmpty()) {
            serieEpisodeRepository.saveAll(episodesToSave);
        }
    }

    private SerieEpisode createEpisodeEntity(Serie serie, SerieSeason season, Map<String, Object> episode) {
        SerieEpisode serieEpisode = new SerieEpisode();
        serieEpisode.setEpisodeId(Long.valueOf(episode.get("id").toString()));
        serieEpisode.setSerie(serie);
        serieEpisode.setSeason(season);
        serieEpisode.setEpisodeNumber((Integer) episode.get("episode_number"));

        if (episode.get("runtime") != null) {
            serieEpisode.setRuntime((Integer) episode.get("runtime"));
        }

        String airDate = (String) episode.get("air_date");
        if (StringUtils.hasText(airDate)) {
            try {
                serieEpisode.setAirDate(Date.valueOf(airDate));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return serieEpisode;
    }

    private void publishDataLoadedEvent(UUID userId, Long serieId, Long contextId, SerieContext context, EventAction action, WatchStatus watchStatus) {
        SerieDataLoadedEvent event = switch (context) {
            case SERIE -> new SerieDataLoadedEvent.SerieEvent(this, serieId, userId, action, watchStatus);
            case SEASON -> new SerieDataLoadedEvent.SeasonEvent(this, serieId, contextId, userId, action, watchStatus);
            case EPISODE ->
                    new SerieDataLoadedEvent.EpisodeEvent(this, serieId, contextId, userId, action, watchStatus);
        };
        eventPublisherService.publishSerieDataLoadedEvent(event);
    }

    private void saveOrUpdateSerie(Long tmdbId, Map<String, Object> serieDetails) {
        Serie serie = serieRepository.findByTmdbId(tmdbId).orElse(new Serie());
        serie.setTmdbId(tmdbId);

        String firstAirDate = (String) serieDetails.get("first_air_date");
        if (StringUtils.hasText(firstAirDate)) {
            try {
                serie.setFirstAirDate(Date.valueOf(firstAirDate));
            } catch (IllegalArgumentException ignored) {
            }
        }

        serie.setPosterPath((String) serieDetails.get("poster_path"));

        serieRepository.save(serie);
    }

    private void saveSerieTranslation(Long tmdbId, String language, Map<String, Object> serieDetails) {
        SerieTranslationId id = new SerieTranslationId();
        id.setTmdbId(tmdbId);
        id.setLanguage(language);

        if (serieTranslationRepository.existsById(id)) return;

        SerieTranslation translation = new SerieTranslation();
        translation.setId(id);
        translation.setName((String) serieDetails.get("name"));
        translation.setOverview((String) serieDetails.get("overview"));
        serieTranslationRepository.save(translation);
    }
}