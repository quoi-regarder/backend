package fr.quoi_regarder.dto.serie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SerieSeasonDto implements Serializable {
    private Long seasonId;
    private Long serieId;
    private Integer seasonNumber;
    private Integer episodeCount;
    private Date airDate;
    private Set<Long> episodeIds;
    private Date createdAt;
    private Date updatedAt;
}