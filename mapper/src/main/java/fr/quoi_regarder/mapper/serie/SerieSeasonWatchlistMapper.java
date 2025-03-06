package fr.quoi_regarder.mapper.serie;

import fr.quoi_regarder.dto.serie.SerieSeasonWatchlistDto;
import fr.quoi_regarder.entity.serie.SerieSeasonWatchlist;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SerieSeasonWatchlistMapper {
    @Mapping(target = "id.tmdbId", source = "tmdbId")
    @Mapping(target = "id.userId", source = "userId")
    SerieSeasonWatchlist toEntity(SerieSeasonWatchlistDto serieSeasonWatchlistDto);

    @Mapping(target = "tmdbId", source = "id.tmdbId")
    @Mapping(target = "userId", source = "id.userId")
    SerieSeasonWatchlistDto toDto(SerieSeasonWatchlist serieSeasonWatchlist);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "id.tmdbId", ignore = true)
    @Mapping(target = "id.userId", ignore = true)
    SerieSeasonWatchlist partialUpdate(SerieSeasonWatchlistDto serieSeasonWatchlistDto, @MappingTarget SerieSeasonWatchlist serieSeasonWatchlist);
}