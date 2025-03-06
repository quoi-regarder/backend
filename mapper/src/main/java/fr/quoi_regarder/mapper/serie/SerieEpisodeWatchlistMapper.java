package fr.quoi_regarder.mapper.serie;

import fr.quoi_regarder.dto.serie.SerieEpisodeWatchlistDto;
import fr.quoi_regarder.entity.serie.SerieEpisodeWatchlist;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SerieEpisodeWatchlistMapper {
    @Mapping(target = "id.tmdbId", source = "tmdbId")
    @Mapping(target = "id.userId", source = "userId")
    SerieEpisodeWatchlist toEntity(SerieEpisodeWatchlistDto serieEpisodeWatchlistDto);

    @Mapping(target = "tmdbId", source = "id.tmdbId")
    @Mapping(target = "userId", source = "id.userId")
    SerieEpisodeWatchlistDto toDto(SerieEpisodeWatchlist serieEpisodeWatchlist);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "id.tmdbId", ignore = true)
    @Mapping(target = "id.userId", ignore = true)
    SerieEpisodeWatchlist partialUpdate(SerieEpisodeWatchlistDto serieEpisodeWatchlistDto, @MappingTarget SerieEpisodeWatchlist serieEpisodeWatchlist);
}