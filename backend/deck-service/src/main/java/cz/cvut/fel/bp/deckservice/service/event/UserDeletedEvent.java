package cz.cvut.fel.bp.deckservice.service.event;

import java.util.UUID;

public record UserDeletedEvent(
        UUID userId
) {}
