package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.entity.serie.SerieWatchlist;
import fr.quoi_regarder.entity.serie.id.SerieWatchlistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SerieWatchlistRepository extends JpaRepository<SerieWatchlist, SerieWatchlistId> {
    @Query("SELECT se.id.tmdbId FROM SerieWatchlist se WHERE se.id.userId = :userId AND se.status = :status")
    List<Long> findSerieIdsByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") WatchStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM SerieWatchlist se WHERE se.id.userId = :userId AND se.id.tmdbId = :serieId")
    void deleteByUserIdAndTmdbId(@Param("userId") UUID userId, @Param("serieId") Long serieId);

    Optional<SerieWatchlist> findByIdUserIdAndIdTmdbId(UUID userId, Long tmdbId);
}