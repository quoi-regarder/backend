package fr.quoi_regarder.event.serie;

import fr.quoi_regarder.commons.enums.EventAction;
import fr.quoi_regarder.commons.enums.SerieContext;
import fr.quoi_regarder.commons.enums.WatchStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public abstract class SerieDataLoadedEvent {
    private final Object source;
    private final Long serieId;
    private final Long contextId;
    private final UUID userId;
    private final EventAction action;
    private final WatchStatus watchStatus;
    private final SerieContext serieContext;

    public static class SerieEvent extends SerieDataLoadedEvent {
        public SerieEvent(Object source, Long serieId, UUID userId, EventAction action, WatchStatus watchStatus) {
            super(source, serieId, null, userId, action, watchStatus, SerieContext.SERIE);
        }
    }

    public static class SeasonEvent extends SerieDataLoadedEvent {
        public SeasonEvent(Object source, Long serieId, Long seasonId, UUID userId, EventAction action, WatchStatus watchStatus) {
            super(source, serieId, seasonId, userId, action, watchStatus, SerieContext.SEASON);
        }
    }

    public static class EpisodeEvent extends SerieDataLoadedEvent {
        public EpisodeEvent(Object source, Long serieId, Long episodeId, UUID userId, EventAction action, WatchStatus watchStatus) {
            super(source, serieId, episodeId, userId, action, watchStatus, SerieContext.EPISODE);
        }
    }
}