package fr.quoi_regarder.event;

import fr.quoi_regarder.event.serie.SerieDataLoadedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventPublisherService {
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void publishSerieDataLoadedEvent(SerieDataLoadedEvent event) {
        eventPublisher.publishEvent(event);
    }
}