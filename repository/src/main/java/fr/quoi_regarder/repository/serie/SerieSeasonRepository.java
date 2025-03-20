package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.entity.serie.SerieSeason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerieSeasonRepository extends JpaRepository<SerieSeason, Long> {
}