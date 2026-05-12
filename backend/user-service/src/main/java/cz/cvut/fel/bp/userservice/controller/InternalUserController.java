package cz.cvut.fel.bp.userservice.controller;

import cz.cvut.fel.bp.userservice.dto.UserInfoDTO;
import cz.cvut.fel.bp.userservice.service.fasade.UserServiceFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Internal rest controller.
 * Used within BE.
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserServiceFacade userServiceFacade;

    /**
     * Sends user info DTO based on user principal.
     * @param id UUID of the user
     * @return User info DTO.
     */
    @GetMapping("/{id}")
    public UserInfoDTO getUserInfo(@PathVariable UUID id) {
        log.debug("Get user info request userId={}", id);
        UserInfoDTO userInfo = userServiceFacade.getUserById(id);
        log.debug("Get user info completed userId={}", id);
        return userInfo;
    }

    @GetMapping("/search/id")
    public List<UUID> getUserIdsByKeyword(@RequestParam("keyword") String keyword) {
        log.debug("Search user IDs request keyword={}", keyword);
        List<UUID> userIds = userServiceFacade.searchUserIdsByKeyword(keyword);
        log.debug("Search user IDs completed keyword={}, resultCount={}", keyword, userIds.size());
        return userIds;
    }

    @GetMapping("/search/names")
    public Map<UUID, String> getUsernamesByIds(@RequestParam("authorIds") Set<UUID> userIds) {
        int requestedIdsCount = userIds == null ? 0 : userIds.size();
        log.debug("Get usernames by IDs request idCount={}", requestedIdsCount);
        Map<UUID, String> usernames = userServiceFacade.getUsernamesByIds(userIds);
        log.debug("Get usernames by IDs completed idCount={}, resultCount={}", requestedIdsCount, usernames.size());
        return usernames;
    }
}
