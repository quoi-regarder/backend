package fr.quoi_regarder.service;

import fr.quoi_regarder.commons.enums.LanguageIsoType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TmdbService {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${tmdb.api.url}")
    private String baseUrl;
    @Value("${tmdb.api.key}")
    private String apiKey;

    public Map fetchMovieDetails(Long tmdbId, String language) {
        String url = String.format("%s/movie/%d?api_key=%s&language=%s",
                baseUrl, tmdbId, apiKey, language);

        return restTemplate.getForObject(url, Map.class);
    }

    public Map fetchSerieDetails(Long tmdbId, LanguageIsoType language) {
        String url = String.format("%s/tv/%d?api_key=%s&language=%s",
                baseUrl, tmdbId, apiKey, language);

        return restTemplate.getForObject(url, Map.class);
    }

    public Map fetchSeasonDetails(Long serieId, Integer seasonNumber) {
        String url = String.format("%s/tv/%d/season/%d?api_key=%s",
                baseUrl, serieId, seasonNumber, apiKey);

        return restTemplate.getForObject(url, Map.class);
    }
}