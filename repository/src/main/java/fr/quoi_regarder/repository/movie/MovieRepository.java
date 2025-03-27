package fr.quoi_regarder.repository.movie;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.entity.movie.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query(
            value = "SELECT m FROM Movie m JOIN m.watchlist w WHERE w.user.id = :id AND w.status = :status ORDER BY w.createdAt DESC",
            countQuery = "SELECT COUNT(m) FROM Movie m JOIN m.watchlist w WHERE w.user.id = :id AND w.status = :status"
    )
    Page<Movie> findByWatchlistUserIdAndWatchlistStatus(UUID id, WatchStatus status, Pageable pageable);

    Optional<Movie> findByTmdbId(Long tmdbId);
}