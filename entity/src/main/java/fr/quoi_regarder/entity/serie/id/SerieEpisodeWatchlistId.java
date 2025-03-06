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
public class SerieEpisodeWatchlistId implements Serializable {
    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(name = "user_id")
    private UUID userId;
}
