package fr.quoi_regarder.mapper.serie;

import fr.quoi_regarder.dto.serie.SerieDto;
import fr.quoi_regarder.entity.serie.Serie;
import fr.quoi_regarder.entity.serie.SerieTranslation;
import fr.quoi_regarder.entity.serie.id.SerieTranslationId;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SerieMapper {
    Serie toEntity(SerieDto serieDto);

    @Mapping(target = "translationIds", source = "translations", qualifiedByName = "mapTranslationsIds")
    SerieDto toDto(Serie serie);

    Serie partialUpdate(SerieDto serieDto, @MappingTarget Serie serie);

    @Named("mapTranslationsIds")
    default Set<Long> mapTranslationsIds(Set<SerieTranslation> translations) {
        return translations.stream()
                .map(SerieTranslation::getId)
                .map(SerieTranslationId::getTmdbId)
                .collect(Collectors.toSet());
    }
}