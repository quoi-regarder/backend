package fr.quoi_regarder.entity.serie;

import fr.quoi_regarder.commons.enums.WatchStatus;
import fr.quoi_regarder.entity.serie.id.SerieSeasonWatchlistId;
import fr.quoi_regarder.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "serie_season_watchlist")
public class SerieSeasonWatchlist implements Serializable {
    @EmbeddedId
    private SerieSeasonWatchlistId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_id", insertable = false, updatable = false)
    private SerieSeason serieSeason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WatchStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}
