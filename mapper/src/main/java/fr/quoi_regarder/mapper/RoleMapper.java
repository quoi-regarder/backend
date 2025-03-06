package fr.quoi_regarder.mapper;

import fr.quoi_regarder.dto.RoleDto;
import fr.quoi_regarder.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleMapper {
    Role toEntity(RoleDto roleDto);

    RoleDto toDto(Role role);

    Role partialUpdate(RoleDto roleDto, @MappingTarget Role role);
}