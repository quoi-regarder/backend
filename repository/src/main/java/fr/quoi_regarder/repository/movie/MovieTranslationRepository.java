package fr.quoi_regarder.repository.movie;

import fr.quoi_regarder.entity.movie.MovieTranslation;
import fr.quoi_regarder.entity.movie.id.MovieTranslationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieTranslationRepository extends JpaRepository<MovieTranslation, MovieTranslationId> {


    Optional<MovieTranslation> findByIdTmdbIdAndIdLanguage(Long tmdbId, String language);
}