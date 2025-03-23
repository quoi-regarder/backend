package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.entity.serie.SerieEpisodeWatchlist;
import fr.quoi_regarder.entity.serie.id.SerieEpisodeWatchlistId;
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
public interface SerieEpisodeWatchlistRepository extends JpaRepository<SerieEpisodeWatchlist, SerieEpisodeWatchlistId> {
    @Query(
            value = "SELECT SUM(e.runtime) FROM SerieEpisode e JOIN SerieEpisodeWatchlist sew ON e.episodeId = sew.serieEpisode.episodeId WHERE sew.id.userId = :userId AND sew.status = 'watched'"
    )
    Long calculateTotalRuntimeForUser(UUID userId);

    @Query("SELECT e.id.tmdbId FROM SerieEpisodeWatchlist e WHERE e.id.userId = :userId AND e.serieEpisode.serie.tmdbId = :serieTmdbId AND e.status = :status")
    List<Long> findEpisodeByUserIdAndSerieTmdbIdAndStatus(@Param("userId") UUID userId, @Param("serieTmdbId") Long serieTmdbId, @Param("status") WatchStatus status);

    List<SerieEpisodeWatchlist> findByIdUserIdAndSerieEpisodeSeasonSeasonId(UUID userId, Long seasonId);

    Optional<SerieEpisodeWatchlist> findByIdUserIdAndIdTmdbId(UUID userId, Long tmdbId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SerieEpisodeWatchlist e WHERE e.id.userId = :userId AND e.serieEpisode.serie.tmdbId = :serieId")
    void deleteByUserIdAndTmdbId(@Param("userId") UUID userId, @Param("serieId") Long serieId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SerieEpisodeWatchlist e WHERE e.id.userId = :userId AND e.serieEpisode.season.seasonId = :seasonId")
    void deleteByUserIdAndSeasonId(@Param("userId") UUID userId, @Param("seasonId") Long seasonId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SerieEpisodeWatchlist e WHERE e.id.userId = :userId AND e.serieEpisode.episodeId = :episodeId")
    void deleteByUserIdAndEpisodeId(@Param("userId") UUID userId, @Param("episodeId") Long episodeId);
}