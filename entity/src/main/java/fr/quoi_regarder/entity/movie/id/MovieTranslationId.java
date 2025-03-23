package fr.quoi_regarder.entity.movie.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
public class MovieTranslationId implements Serializable {
    @Column(name = "tmdb_id", nullable = false)
    private Long tmdbId;

    @Column(name = "language", nullable = false)
    private String language;
}