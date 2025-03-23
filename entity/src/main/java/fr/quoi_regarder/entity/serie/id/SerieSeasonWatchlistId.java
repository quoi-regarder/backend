package fr.quoi_regarder.entity.serie.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
public class SerieSeasonWatchlistId implements Serializable {
    @Column(name = "tmdb_id", nullable = false)
    private Long tmdbId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
}
