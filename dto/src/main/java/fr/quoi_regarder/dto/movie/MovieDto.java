package fr.quoi_regarder.dto.movie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDto implements Serializable {
    private Long tmdbId;
    private Set<Long> translationIds;
    private Integer runtime;
    private Date releaseDate;
    private String posterPath;
    private Date createdAt;
    private Date updatedAt;
}