package fr.quoi_regarder.entity.serie;

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
@Table(name = "serie_seasons")
public class SerieSeason implements Serializable, Comparable<SerieSeason> {
    @Id
    @Column(name = "season_id", nullable = false)
    private Long seasonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "serie_id", nullable = false)
    private Serie serie;

    @Column(name = "season_number")
    private Integer seasonNumber;

    @Column(name = "episode_count")
    private Integer episodeCount;

    @Column(name = "air_date")
    private Date airDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("episodeNumber ASC")
    private Set<SerieEpisode> episodes;

    @Override
    public int compareTo(SerieSeason other) {
        return this.seasonNumber.compareTo(other.seasonNumber);
    }
}
