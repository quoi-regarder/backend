package fr.quoi_regarder.dto.movie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieDto implements Serializable {
    private Long tmdbId;
    private Integer runtime;
    private Date releaseDate;
    private String posterPath;
    private String title;
    private String overview;
    private Date createdAt;
    private Date updatedAt;
}