package fr.quoi_regarder.dto.serie;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SerieEpisodeDto implements Serializable {
    @NotNull
    private Long episodeId;
    @NotNull
    private Long seasonId;
    @NotNull
    private Long serieId;
    private Integer episodeNumber;
    private Integer runtime;
    private Date airDate;
    private Date createdAt;
    private Date updatedAt;
}