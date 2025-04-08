package fr.quoi_regarder.mapper.serie;

import fr.quoi_regarder.dto.serie.SerieFavoriteDto;
import fr.quoi_regarder.entity.serie.SerieFavorite;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SerieFavoriteMapper {
    @Mapping(target = "id.tmdbId", source = "tmdbId")
    @Mapping(target = "id.userId", source = "userId")
    SerieFavorite toEntity(SerieFavoriteDto serieFavoriteDto);

    @Mapping(target = "tmdbId", source = "id.tmdbId")
    @Mapping(target = "userId", source = "id.userId")
    SerieFavoriteDto toDto(SerieFavorite serieFavorite);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "id.tmdbId", ignore = true)
    @Mapping(target = "id.userId", ignore = true)
    SerieFavorite partialUpdate(SerieFavoriteDto serieFavoriteDto, @MappingTarget SerieFavorite serieFavorite);
}