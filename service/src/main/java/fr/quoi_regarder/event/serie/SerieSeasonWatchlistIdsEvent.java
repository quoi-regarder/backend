package fr.quoi_regarder.event.serie;

import fr.quoi_regarder.commons.enums.SseEventType;
import fr.quoi_regarder.event.UserEvent;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class SerieSeasonWatchlistIdsEvent extends UserEvent {
    private final Map<String, List<Long>> watchlistIds;

    public SerieSeasonWatchlistIdsEvent(Object source, UUID userId, Map<String, List<Long>> watchlistIds) {
        super(source, userId, SseEventType.SERIE_SEASON_WATCHLIST_IDS_UPDATE);
        this.watchlistIds = watchlistIds;
    }

    @Override
    public Object getEventData() {
        return watchlistIds;
    }
}
