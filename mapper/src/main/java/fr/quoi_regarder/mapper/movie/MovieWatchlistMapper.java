package fr.quoi_regarder.mapper.movie;

import fr.quoi_regarder.dto.movie.MovieWatchlistDto;
import fr.quoi_regarder.entity.movie.MovieWatchlist;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovieWatchlistMapper {
    @Mapping(target = "id.tmdbId", source = "tmdbId")
    @Mapping(target = "id.userId", source = "userId")
    MovieWatchlist toEntity(MovieWatchlistDto movieWatchlistDto);

    @Mapping(target = "tmdbId", source = "id.tmdbId")
    @Mapping(target = "userId", source = "id.userId")
    MovieWatchlistDto toDto(MovieWatchlist movieWatchlist);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "id.tmdbId", ignore = true)
    @Mapping(target = "id.userId", ignore = true)
    MovieWatchlist partialUpdate(MovieWatchlistDto movieWatchlistDto, @MappingTarget MovieWatchlist movieWatchlist);
}