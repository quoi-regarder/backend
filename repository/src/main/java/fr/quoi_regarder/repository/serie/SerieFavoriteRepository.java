package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.entity.serie.SerieFavorite;
import fr.quoi_regarder.entity.serie.id.SerieFavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SerieFavoriteRepository extends JpaRepository<SerieFavorite, SerieFavoriteId> {
    @Query("SELECT sf.id.tmdbId FROM SerieFavorite sf WHERE sf.id.userId = :userId")
    List<Long> findSerieIdsByUserId(UUID userId);
}