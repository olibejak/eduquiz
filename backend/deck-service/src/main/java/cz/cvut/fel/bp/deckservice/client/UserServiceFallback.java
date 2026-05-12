package cz.cvut.fel.bp.deckservice.client;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class UserServiceFallback implements UserServiceClient {

    @Override
    public List<UUID> getUserIdsByName(String keyword) {
        return List.of();
    }

    @Override
    public Map<UUID, String> getUsernamesByIds(Set<UUID> authorIds) {
        return Map.of();
    }
}
