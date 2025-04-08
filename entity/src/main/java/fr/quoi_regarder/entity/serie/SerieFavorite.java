package fr.quoi_regarder.entity.serie;

import fr.quoi_regarder.entity.serie.id.SerieFavoriteId;
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
@Table(name = "serie_favorites")
public class SerieFavorite implements Serializable {
    @EmbeddedId
    private SerieFavoriteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_id", insertable = false, updatable = false)
    private Serie serie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}
