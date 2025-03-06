package fr.quoi_regarder.dto.movie;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovieTranslationDto implements Serializable {
    @NotNull
    private Long tmdbId;
    @NotNull
    private String language;
    private String title;
    private String overview;
    @NotNull
    private Date createdAt;
    private Date updatedAt;
}