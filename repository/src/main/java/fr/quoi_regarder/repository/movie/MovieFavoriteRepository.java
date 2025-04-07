package fr.quoi_regarder.repository.movie;

import fr.quoi_regarder.entity.movie.MovieFavorite;
import fr.quoi_regarder.entity.movie.id.MovieFavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MovieFavoriteRepository extends JpaRepository<MovieFavorite, MovieFavoriteId> {
    @Query("SELECT mf.id.tmdbId FROM MovieFavorite mf WHERE mf.id.userId = :userId")
    List<Long> findMovieIdsByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM MovieFavorite mf WHERE mf.id.userId = :userId AND mf.id.tmdbId = :tmdbId")
    void deleteByUserIdAndTmdbId(UUID userId, Long tmdbId);
}