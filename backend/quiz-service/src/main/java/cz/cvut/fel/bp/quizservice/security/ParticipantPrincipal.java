package cz.cvut.fel.bp.quizservice.security;

import java.security.Principal;

public class ParticipantPrincipal implements Principal {

    private final String participantId;

    public ParticipantPrincipal(String participantId) {
        this.participantId = participantId;
    }

    @Override
    public String getName() {
        return this.participantId;
    }
}
