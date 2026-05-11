package cz.cvut.fel.bp.deckservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@FeignClient(name = "user-service", url = "${microservices.user-service.url}")
public interface UserServiceClient {

    @GetMapping("/api/internal/users/search/id")
    List<UUID> getUserIdsByName(@RequestParam("keyword") String keyword);

    @GetMapping("/api/internal/users/search/names")
    Map<UUID, String> getUsernamesByIds(@RequestParam("authorIds") Set<UUID> authorIds);
}
