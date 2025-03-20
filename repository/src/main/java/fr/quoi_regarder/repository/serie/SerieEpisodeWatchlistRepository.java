package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.entity.serie.SerieEpisodeWatchlist;
import fr.quoi_regarder.entity.serie.id.SerieEpisodeWatchlistId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerieEpisodeWatchlistRepository extends JpaRepository<SerieEpisodeWatchlist, SerieEpisodeWatchlistId> {
}