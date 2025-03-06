package fr.quoi_regarder.mapper.movie;

import fr.quoi_regarder.dto.movie.MovieDto;
import fr.quoi_regarder.entity.movie.Movie;
import fr.quoi_regarder.entity.movie.MovieTranslation;
import fr.quoi_regarder.entity.movie.id.MovieTranslationId;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovieMapper {
    Movie toEntity(MovieDto movieDto);

    @Mapping(target = "translationIds", source = "translations", qualifiedByName = "mapTranslationsIds")
    MovieDto toDto(Movie movie);

    Movie partialUpdate(MovieDto movieDto, @MappingTarget Movie movie);

    @Named("mapTranslationsIds")
    default Set<Long> mapTranslationsIds(Set<MovieTranslation> translations) {
        return translations.stream()
                .map(MovieTranslation::getId)
                .map(MovieTranslationId::getTmdbId)
                .collect(Collectors.toSet());
    }
}