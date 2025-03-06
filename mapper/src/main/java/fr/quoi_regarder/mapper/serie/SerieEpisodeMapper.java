package fr.quoi_regarder.mapper.serie;

import fr.quoi_regarder.dto.serie.SerieEpisodeDto;
import fr.quoi_regarder.entity.serie.SerieEpisode;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SerieEpisodeMapper {
    SerieEpisode toEntity(SerieEpisodeDto serieEpisodeDto);

    @Mapping(target = "seasonId", source = "season.seasonId")
    @Mapping(target = "serieId", source = "serie.tmdbId")
    SerieEpisodeDto toDto(SerieEpisode serieEpisode);

    SerieEpisode partialUpdate(SerieEpisodeDto serieEpisodeDto, @MappingTarget SerieEpisode serieEpisode);
}