package fr.quoi_regarder.entity.movie;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieFavoriteDto implements Serializable {
    @NotNull
    private Long tmdbId;
    @NotNull
    private UUID userId;
    private Date createdAt;
    private Date updatedAt;
}