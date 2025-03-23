package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.entity.serie.SerieTranslation;
import fr.quoi_regarder.entity.serie.id.SerieTranslationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SerieTranslationRepository extends JpaRepository<SerieTranslation, SerieTranslationId> {
    Optional<SerieTranslation> findByIdTmdbIdAndIdLanguage(Long tmdbId, String language);
}