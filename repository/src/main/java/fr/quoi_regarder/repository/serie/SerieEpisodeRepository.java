package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.entity.serie.SerieEpisode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SerieEpisodeRepository extends JpaRepository<SerieEpisode, Long> {
    List<SerieEpisode> findBySerieTmdbId(Long tmdbId);

    List<SerieEpisode> findBySeasonSeasonId(Long seasonId);

    long countBySeasonSeasonId(Long seasonId);

}