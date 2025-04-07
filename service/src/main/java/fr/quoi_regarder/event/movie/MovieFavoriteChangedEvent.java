package fr.quoi_regarder.event.movie;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class MovieFavoriteChangedEvent extends ApplicationEvent {
    private final UUID userId;

    public MovieFavoriteChangedEvent(Object source, UUID userId) {
        super(source);
        this.userId = userId;
    }
}
