package fr.quoi_regarder.event.movie;

import fr.quoi_regarder.commons.enums.SseEventType;
import fr.quoi_regarder.event.UserEvent;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class MovieWatchlistIdsEvent extends UserEvent {
    private final Map<String, List<Long>> watchlistIds;

    public MovieWatchlistIdsEvent(Object source, UUID userId, Map<String, List<Long>> watchlistIds) {
        super(source, userId, SseEventType.MOVIE_WATCHLIST_IDS_UPDATE);
        this.watchlistIds = watchlistIds;
    }

    @Override
    public Object getEventData() {
        return watchlistIds;
    }
}
