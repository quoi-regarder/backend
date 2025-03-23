package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.entity.serie.Serie;
import fr.quoi_regarder.entity.serie.SerieSeason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerieSeasonRepository extends JpaRepository<SerieSeason, Long> {
    SerieSeason findBySerieAndSeasonNumber(Serie serie, Integer seasonNumber);

    List<SerieSeason> findBySerieTmdbId(Long tmdbId);

    long countBySerieTmdbId(Long tmdbId);

    Optional<SerieSeason> findByEpisodesEpisodeId(Long episodeId);
}