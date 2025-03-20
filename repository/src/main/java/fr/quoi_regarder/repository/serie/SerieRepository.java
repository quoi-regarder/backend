package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.entity.serie.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerieRepository extends JpaRepository<Serie, Long> {
}