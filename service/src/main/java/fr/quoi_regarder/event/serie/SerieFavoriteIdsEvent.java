package fr.quoi_regarder.event.serie;

import fr.quoi_regarder.commons.enums.SseEventType;
import fr.quoi_regarder.event.UserEvent;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class SerieFavoriteIdsEvent extends UserEvent {
    private final Map<String, List<Long>> favoriteIds;

    public SerieFavoriteIdsEvent(Object source, UUID userId, Map<String, List<Long>> favoriteIds) {
        super(source, userId, SseEventType.SERIE_FAVORITE_IDS_UPDATE);
        this.favoriteIds = favoriteIds;
    }

    @Override
    public Object getEventData() {
        return favoriteIds;
    }
}
