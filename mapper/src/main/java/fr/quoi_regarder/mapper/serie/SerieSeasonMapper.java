package fr.quoi_regarder.mapper.serie;

import fr.quoi_regarder.dto.serie.SerieSeasonDto;
import fr.quoi_regarder.entity.serie.SerieEpisode;
import fr.quoi_regarder.entity.serie.SerieSeason;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SerieSeasonMapper {
    SerieSeason toEntity(SerieSeasonDto serieSeasonDto);

    @Mapping(target = "serieId", source = "serie.tmdbId")
    @Mapping(target = "episodeIds", source = "episodes", qualifiedByName = "mapEpisodeIds")
    SerieSeasonDto toDto(SerieSeason serieSeason);

    SerieSeason partialUpdate(SerieSeasonDto serieSeasonDto, @MappingTarget SerieSeason serieSeason);

    @Named("mapEpisodeIds")
    default Set<Long> mapEpisodeIds(Set<SerieEpisode> episodes) {
        return episodes.stream()
                .map(SerieEpisode::getEpisodeId)
                .collect(Collectors.toSet());
    }
}