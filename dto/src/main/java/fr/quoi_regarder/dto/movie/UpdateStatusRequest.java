package fr.quoi_regarder.dto.movie;

import fr.quoi_regarder.commons.enums.WatchStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {
    private WatchStatus status;
}
