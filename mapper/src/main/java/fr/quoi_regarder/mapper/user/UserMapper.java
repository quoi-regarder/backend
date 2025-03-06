package fr.quoi_regarder.mapper.user;

import fr.quoi_regarder.dto.user.UserDto;
import fr.quoi_regarder.entity.user.User;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toEntity(UserDto userDto);

    @Mapping(source = "role.id", target = "roleId")
    UserDto toDto(User user);

    User partialUpdate(UserDto userDto, @MappingTarget User user);
}