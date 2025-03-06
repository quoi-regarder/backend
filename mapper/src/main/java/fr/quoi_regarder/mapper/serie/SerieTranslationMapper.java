package fr.quoi_regarder.mapper.serie;

import fr.quoi_regarder.dto.serie.SerieTranslationDto;
import fr.quoi_regarder.entity.serie.SerieTranslation;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SerieTranslationMapper {
    @Mapping(target = "id.tmdbId", source = "tmdbId")
    @Mapping(target = "id.language", source = "language")
    SerieTranslation toEntity(SerieTranslationDto serieTranslationDto);

    @Mapping(target = "tmdbId", source = "id.tmdbId")
    @Mapping(target = "language", source = "id.language")
    SerieTranslationDto toDto(SerieTranslation serieTranslation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "id.tmdbId", ignore = true)
    @Mapping(target = "id.language", ignore = true)
    SerieTranslation partialUpdate(SerieTranslationDto serieTranslationDto, @MappingTarget SerieTranslation serieTranslation);
}