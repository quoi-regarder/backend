package fr.quoi_regarder.mapper;

import fr.quoi_regarder.dto.TokenDto;
import fr.quoi_regarder.entity.Token;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TokenMapper {
    Token toEntity(TokenDto tokenDto);

    TokenDto toDto(Token token);

    Token partialUpdate(TokenDto tokenDto, @MappingTarget Token token);
}