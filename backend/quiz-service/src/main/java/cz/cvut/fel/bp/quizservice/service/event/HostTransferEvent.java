package cz.cvut.fel.bp.quizservice.service.event;

public record HostTransferEvent(
        String lobbyPin,
        Long oldHostId,
        Long newHostId
) {}
