package fr.quoi_regarder.entity.serie;

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
@Table(name = "serie_episodes")
public class SerieEpisode implements Serializable, Comparable<SerieEpisode> {
    @Id
    @Column(name = "episode_id", nullable = false)
    private Long episodeId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private SerieSeason season;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "serie_id", nullable = false)
    private Serie serie;

    @Column(name = "episode_number")
    private Integer episodeNumber;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "air_date")
    private Date airDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Override
    public int compareTo(SerieEpisode other) {
        return this.episodeNumber.compareTo(other.episodeNumber);
    }
}
