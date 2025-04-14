package fr.quoi_regarder.mapper;

import fr.quoi_regarder.dto.ViewingDetailsDto;
import fr.quoi_regarder.entity.ViewingDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ViewingDetailsMapper {
    ViewingDetails toEntity(ViewingDetailsDto viewingDetailsDto);

    ViewingDetailsDto toDto(ViewingDetails viewingDetails);

    ViewingDetails partialUpdate(ViewingDetailsDto viewingDetailsDto, @MappingTarget ViewingDetails viewingDetails);
}