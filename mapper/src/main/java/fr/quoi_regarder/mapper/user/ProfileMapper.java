package fr.quoi_regarder.mapper.user;

import fr.quoi_regarder.dto.user.ProfileDto;
import fr.quoi_regarder.dto.user.UpdateProfileDto;
import fr.quoi_regarder.entity.user.Profile;
import fr.quoi_regarder.entity.user.User;
import fr.quoi_regarder.repository.user.UserRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ProfileMapper {
    @Autowired
    private UserRepository userRepository;

    public abstract Profile toEntity(ProfileDto profileDto);

    @Mapping(target = "userId", source = "user.id")
    public abstract ProfileDto toDto(Profile profile);

    public abstract Profile partialUpdate(@MappingTarget Profile profile, ProfileDto profileDto);

    public abstract void partialUpdate(@MappingTarget Profile profile, UpdateProfileDto updateProfileDto);

    @AfterMapping
    public void afterMapping(@MappingTarget ProfileDto profileDto, Profile profile) {
        User user = userRepository.findById(profile.getUserId()).orElse(null);

        if (user != null) {
            profileDto.setEmail(user.getEmail());
        }
    }
}