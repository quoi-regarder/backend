package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.entity.serie.SerieSeasonWatchlist;
import fr.quoi_regarder.entity.serie.id.SerieSeasonWatchlistId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerieSeasonWatchlistRepository extends JpaRepository<SerieSeasonWatchlist, SerieSeasonWatchlistId> {
}