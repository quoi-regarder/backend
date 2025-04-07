package fr.quoi_regarder.event.movie;

import fr.quoi_regarder.commons.enums.SseEventType;
import fr.quoi_regarder.event.UserEvent;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class MovieFavoriteIdsEvent extends UserEvent {
    private final Map<String, List<Long>> favoriteIds;

    public MovieFavoriteIdsEvent(Object source, UUID userId, Map<String, List<Long>> favoriteIds) {
        super(source, userId, SseEventType.MOVIE_FAVORITE_IDS_UPDATE);
        this.favoriteIds = favoriteIds;
    }

    @Override
    public Object getEventData() {
        return favoriteIds;
    }
}
