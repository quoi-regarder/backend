package fr.quoi_regarder.entity;

import fr.quoi_regarder.commons.enums.ContextType;
import fr.quoi_regarder.commons.enums.Emotion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "viewing_details")
public class ViewingDetails implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private Long platformId;

    private Integer rating;

    @Enumerated(EnumType.STRING)
    private Emotion emotion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContextType contextType;

    @Column(nullable = false)
    private Long contextId;

    @Column(nullable = false)
    private UUID userId;
}
