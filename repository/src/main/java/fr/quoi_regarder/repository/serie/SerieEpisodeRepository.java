package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.entity.serie.SerieEpisode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerieEpisodeRepository extends JpaRepository<SerieEpisode, Long> {
}