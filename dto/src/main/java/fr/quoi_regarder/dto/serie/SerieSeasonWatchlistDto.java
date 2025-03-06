package fr.quoi_regarder.dto.serie;

import fr.quoi_regarder.commons.enums.WatchStatus;
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
public class SerieSeasonWatchlistDto implements Serializable {
    @NotNull
    private Long tmdbId;
    @NotNull
    private UUID userId;
    private WatchStatus status;
    private Date createdAt;
    private Date updatedAt;
}