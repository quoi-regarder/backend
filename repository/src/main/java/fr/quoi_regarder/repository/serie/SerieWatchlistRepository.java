package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.entity.serie.SerieWatchlist;
import fr.quoi_regarder.entity.serie.id.SerieWatchlistId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerieWatchlistRepository extends JpaRepository<SerieWatchlist, SerieWatchlistId> {
}