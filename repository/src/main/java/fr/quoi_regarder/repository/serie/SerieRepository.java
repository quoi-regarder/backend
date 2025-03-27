package fr.quoi_regarder.repository.serie;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.entity.serie.Serie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SerieRepository extends JpaRepository<Serie, Long> {
    @Query(
            value = "SELECT s FROM Serie s JOIN s.watchlist w WHERE w.user.id = :id AND w.status = :status ORDER BY w.createdAt DESC",
            countQuery = "SELECT COUNT(s) FROM Serie s JOIN s.watchlist w WHERE w.user.id = :id AND w.status = :status"
    )
    Page<Serie> findByWatchlistUserIdAndWatchlistStatus(UUID id, WatchStatus status, Pageable pageable);

    Optional<Serie> findByTmdbId(Long tmdbId);

    Boolean existsByTmdbId(Long tmdbId);

}