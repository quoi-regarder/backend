package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.entity.serie.SerieSeasonWatchlist;
import fr.quoi_regarder.entity.serie.id.SerieSeasonWatchlistId;
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
public interface SerieSeasonWatchlistRepository extends JpaRepository<SerieSeasonWatchlist, SerieSeasonWatchlistId> {
    @Query("SELECT s.id.tmdbId FROM SerieSeasonWatchlist s WHERE s.id.userId = :userId AND s.serieSeason.serie.tmdbId = :serieTmdbId AND s.status = :status")
    List<Long> findSeasonByUserIdAndSerieTmdbIdAndStatus(
            @Param("userId") UUID userId,
            @Param("serieTmdbId") Long serieTmdbId,
            @Param("status") WatchStatus status
    );

    @Query("SELECT s FROM SerieSeasonWatchlist s WHERE s.id.userId = :userId AND s.serieSeason.serie.tmdbId = :serieTmdbId")
    List<SerieSeasonWatchlist> findSeasonByUserIdAndSerieTmdbId(
            @Param("userId") UUID userId,
            @Param("serieTmdbId") Long serieTmdbId
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM SerieSeasonWatchlist s WHERE s.id.userId = :userId AND s.serieSeason.serie.tmdbId = :serieId")
    void deleteByUserIdAndTmdbId(@Param("userId") UUID userId, @Param("serieId") Long serieId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SerieSeasonWatchlist s WHERE s.id.userId = :userId AND s.serieSeason.seasonId = :seasonId")
    void deleteByUserIdAndSeasonId(@Param("userId") UUID userId, @Param("seasonId") Long seasonId);

    List<SerieSeasonWatchlist> findByIdUserIdAndSerieSeasonSerieTmdbId(UUID userId, Long tmdbId);

    Optional<SerieSeasonWatchlist> findByIdUserIdAndIdTmdbId(UUID userId, Long tmdbId);
}