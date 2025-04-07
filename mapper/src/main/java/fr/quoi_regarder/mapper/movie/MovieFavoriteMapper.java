package fr.quoi_regarder.mapper.movie;

import fr.quoi_regarder.entity.movie.MovieFavorite;
import fr.quoi_regarder.entity.movie.MovieFavoriteDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovieFavoriteMapper {
    @Mapping(target = "id.tmdbId", source = "tmdbId")
    @Mapping(target = "id.userId", source = "userId")
    MovieFavorite toEntity(MovieFavoriteDto movieFavoriteDto);

    @Mapping(target = "tmdbId", source = "id.tmdbId")
    @Mapping(target = "userId", source = "id.userId")
    MovieFavoriteDto toDto(MovieFavorite movieFavorite);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "id.tmdbId", ignore = true)
    @Mapping(target = "id.userId", ignore = true)
    MovieFavorite partialUpdate(MovieFavoriteDto movieFavoriteDto, @MappingTarget MovieFavorite movieFavorite);
}