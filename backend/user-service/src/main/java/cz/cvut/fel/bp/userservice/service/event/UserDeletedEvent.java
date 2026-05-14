package cz.cvut.fel.bp.userservice.service.event;

import java.util.UUID;

public record UserDeletedEvent(UUID userId) {}