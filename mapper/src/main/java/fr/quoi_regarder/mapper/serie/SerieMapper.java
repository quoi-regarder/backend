package fr.quoi_regarder.mapper.serie;

import fr.quoi_regarder.dto.serie.SerieDto;
import fr.quoi_regarder.entity.serie.Serie;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class SerieMapper {
    public abstract Serie toEntity(SerieDto serieDto);

    public abstract SerieDto toDto(Serie serie);

    public abstract Serie partialUpdate(SerieDto serieDto, @MappingTarget Serie serie);
}