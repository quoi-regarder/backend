package fr.quoi_regarder.mapper.serie;

import fr.quoi_regarder.dto.serie.SerieWatchlistDto;
import fr.quoi_regarder.entity.serie.SerieWatchlist;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SerieWatchlistMapper {
    @Mapping(target = "id.tmdbId", source = "tmdbId")
    @Mapping(target = "id.userId", source = "userId")
    SerieWatchlist toEntity(SerieWatchlistDto serieWatchlistDto);

    @Mapping(target = "tmdbId", source = "id.tmdbId")
    @Mapping(target = "userId", source = "id.userId")
    SerieWatchlistDto toDto(SerieWatchlist serieWatchlist);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "id.tmdbId", ignore = true)
    @Mapping(target = "id.userId", ignore = true)
    SerieWatchlist partialUpdate(SerieWatchlistDto serieWatchlistDto, @MappingTarget SerieWatchlist serieWatchlist);
}