package fr.quoi_regarder.mapper.movie;

import fr.quoi_regarder.dto.movie.MovieTranslationDto;
import fr.quoi_regarder.entity.movie.MovieTranslation;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovieTranslationMapper {
    @Mapping(target = "id.tmdbId", source = "tmdbId")
    @Mapping(target = "id.language", source = "language")
    MovieTranslation toEntity(MovieTranslationDto movieTranslationDto);

    @Mapping(target = "tmdbId", source = "id.tmdbId")
    @Mapping(target = "language", source = "id.language")
    MovieTranslationDto toDto(MovieTranslation movieTranslation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "id.tmdbId", ignore = true)
    @Mapping(target = "id.language", ignore = true)
    MovieTranslation partialUpdate(MovieTranslationDto movieTranslationDto, @MappingTarget MovieTranslation movieTranslation);
}