package fr.quoi_regarder.mapper.movie;

import fr.quoi_regarder.dto.movie.MovieDto;
import fr.quoi_regarder.entity.movie.Movie;
import fr.quoi_regarder.entity.movie.MovieTranslation;
import fr.quoi_regarder.repository.movie.MovieTranslationRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class MovieMapper {
    @Autowired
    private MovieTranslationRepository movieTranslationRepository;

    public abstract Movie toEntity(MovieDto movieDto);

    public abstract MovieDto toDto(Movie movie, @Context String language);

    public abstract Movie partialUpdate(MovieDto movieDto, @MappingTarget Movie movie);

    @AfterMapping
    public void afterMapping(@MappingTarget MovieDto movieDto, @Context String language) {
        MovieTranslation movieTranslation = movieTranslationRepository.findByIdTmdbIdAndIdLanguage(movieDto.getTmdbId(), language).orElse(null);

        if (movieTranslation != null) {
            movieDto.setTitle(movieTranslation.getTitle());
            movieDto.setOverview(movieTranslation.getOverview());
        }
    }
}