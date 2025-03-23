package fr.quoi_regarder.mapper.serie;

import fr.quoi_regarder.dto.serie.SerieDto;
import fr.quoi_regarder.entity.serie.Serie;
import fr.quoi_regarder.entity.serie.SerieTranslation;
import fr.quoi_regarder.repository.serie.SerieTranslationRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class SerieMapper {
    @Autowired
    private SerieTranslationRepository serieTranslationRepository;

    public abstract Serie toEntity(SerieDto serieDto);

    public abstract SerieDto toDto(Serie serie, @Context String language);

    public abstract Serie partialUpdate(SerieDto serieDto, @MappingTarget Serie serie);

    @AfterMapping
    public void afterMapping(@MappingTarget SerieDto serieDto, @Context String language) {
        SerieTranslation serieTranslation = serieTranslationRepository.findByIdTmdbIdAndIdLanguage(serieDto.getTmdbId(), language).orElse(null);

        if (serieTranslation != null) {
            serieDto.setName(serieTranslation.getName());
            serieDto.setOverview(serieTranslation.getOverview());
        }
    }
}