package fr.quoi_regarder.entity.movie;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "movies")
public class Movie implements Serializable {
    @Id
    @Column(name = "tmdb_id", nullable = false)
    private Long tmdbId;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MovieTranslation> translations;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MovieWatchlist> watchlist;

    @Column(name = "runtime", nullable = false)
    private Integer runtime;

    @Column(name = "release_date")
    private Date releaseDate;

    @Column(name = "poster_path")
    private String posterPath;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}
