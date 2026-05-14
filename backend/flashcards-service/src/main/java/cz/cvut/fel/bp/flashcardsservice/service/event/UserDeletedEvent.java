package cz.cvut.fel.bp.flashcardsservice.service.event;

import java.util.UUID;

public record UserDeletedEvent(
        UUID userId
) {}
