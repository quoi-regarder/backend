package fr.quoi_regarder.dto.user;

import fr.quoi_regarder.commons.enums.ColorModeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateColorModeDto {
    private ColorModeType colorMode;
}
