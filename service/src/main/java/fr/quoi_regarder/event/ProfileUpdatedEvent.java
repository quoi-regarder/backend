package fr.quoi_regarder.event;

import fr.quoi_regarder.commons.enums.SseEventType;
import fr.quoi_regarder.dto.user.ProfileDto;
import lombok.Getter;

import java.util.UUID;

/**
 * Event fired when a user's profile is updated.
 */
@Getter
public class ProfileUpdatedEvent extends UserEvent {
    private final ProfileDto profile;

    public ProfileUpdatedEvent(Object source, UUID userId, ProfileDto profile) {
        super(source, userId, SseEventType.PROFILE_UPDATE);
        this.profile = profile;
    }

    @Override
    public Object getEventData() {
        return profile;
    }
}