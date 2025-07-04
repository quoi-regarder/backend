package fr.quoi_regarder.service.serie;

import fr.quoi_regarder.commons.enums.LanguageIsoType;
import fr.quoi_regarder.entity.serie.Serie;
import fr.quoi_regarder.entity.serie.SerieEpisode;
import fr.quoi_regarder.entity.serie.SerieSeason;
import fr.quoi_regarder.entity.serie.SerieTranslation;
import fr.quoi_regarder.entity.serie.id.SerieTranslationId;
import fr.quoi_regarder.exception.exceptions.EntityNotExistsException;
import fr.quoi_regarder.repository.serie.SerieEpisodeRepository;
import fr.quoi_regarder.repository.serie.SerieRepository;
import fr.quoi_regarder.repository.serie.SerieSeasonRepository;
import fr.quoi_regarder.repository.serie.SerieTranslationRepository;
import fr.quoi_regarder.service.TmdbService;
import fr.quoi_regarder.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SerieService {
    private final SerieTranslationRepository serieTranslationRepository;
    private final SerieEpisodeRepository serieEpisodeRepository;
    private final SerieSeasonRepository serieSeasonRepository;
    private final SerieRepository serieRepository;
    private final TmdbService tmdbService;
    private final UserService userService;

    @Transactional
    public void loadSerie(UUID userId, Long serieId, SerieLoadCallback callback) {
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
                        return null;
                    });

            CompletableFuture<Void> translationsFuture = CompletableFuture.runAsync(() ->
                            processTranslations(serieId, userLanguage))
                    .exceptionally(ex -> {
                        return null;
                    });

            CompletableFuture.allOf(episodesFuture, translationsFuture)
                    .thenRun(() -> {
                        callback.onSerieLoaded(userId, serieId);
                    });
        } else {
            throw new EntityNotExistsException(Serie.class, "Failed to save series with TMDB ID: " + serieId);
        }
    }

    private void processSerieEpisodes(Long serieId, Map<String, Object> serieDetails) {
        Serie serie = serieRepository.findByTmdbId(serieId).orElse(null);
        if (serie == null) return;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> seasons = (List<Map<String, Object>>) serieDetails.get("seasons");
        if (seasons == null || seasons.isEmpty()) return;

        seasons.parallelStream().forEach(season -> processSingleSeason(serie, season));
    }

    private void processTranslations(Long serieId, String defaultLanguage) {
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

    private void saveOrUpdateSerie(Long tmdbId, Map<String, Object> serieDetails) {
        boolean exists = serieRepository.existsByTmdbId(tmdbId);

        String posterPath = (String) serieDetails.get("poster_path");

        Date firstAirDate = null;
        String firstAirDateStr = (String) serieDetails.get("first_air_date");
        if (StringUtils.hasText(firstAirDateStr)) {
            try {
                firstAirDate = Date.valueOf(firstAirDateStr);
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (exists) {
            serieRepository.updateSerie(tmdbId, posterPath, firstAirDate);
        } else {
            // Créer une nouvelle série
            Serie serie = new Serie();
            serie.setTmdbId(tmdbId);
            serie.setPosterPath(posterPath);
            serie.setFirstAirDate(firstAirDate);
            serieRepository.save(serie);
        }
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

    @FunctionalInterface
    public interface SerieLoadCallback {
        void onSerieLoaded(UUID userId, Long serieId);
    }
}