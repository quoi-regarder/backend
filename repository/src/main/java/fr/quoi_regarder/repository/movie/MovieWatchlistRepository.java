package fr.quoi_regarder.repository.movie;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.entity.movie.MovieWatchlist;
import fr.quoi_regarder.entity.movie.id.MovieWatchlistId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieWatchlistRepository extends JpaRepository<MovieWatchlist, MovieWatchlistId> {
    @Query("SELECT mw.id.tmdbId FROM MovieWatchlist mw WHERE mw.id.userId = :userId AND mw.status = :status")
    List<Long> findMovieIdsByUserIdAndStatus(UUID userId, WatchStatus status);

    @Query(
            value = "SELECT SUM(m.runtime) FROM Movie m JOIN MovieWatchlist mw ON m.tmdbId = mw.id.tmdbId WHERE mw.id.userId = :userId AND mw.status = :status")
    Long getTotalRuntimeForUserAndStatus(UUID userId, WatchStatus status);

    Optional<MovieWatchlist> findByIdTmdbIdAndIdUserId(Long tmdbId, UUID userId);

    @Modifying
    @Query("DELETE FROM MovieWatchlist mw WHERE mw.id.userId = :userId AND mw.id.tmdbId = :tmdbId")
    void deleteByUserIdAndTmdbId(@Param("userId") UUID userId, @Param("tmdbId") Long tmdbId);
}