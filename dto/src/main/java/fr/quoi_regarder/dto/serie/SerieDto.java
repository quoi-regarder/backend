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
public class SerieDto implements Serializable {
    private Long tmdbId;
    private Date firstAirDate;
    private String posterPath;
    private String name;
    private String overview;
    private Set<Long> seasonIds;
    private Set<Long> episodeIds;
    private Date createdAt;
    private Date updatedAt;
}