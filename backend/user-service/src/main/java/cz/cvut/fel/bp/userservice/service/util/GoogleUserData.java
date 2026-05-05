package cz.cvut.fel.bp.userservice.service.util;

import lombok.Builder;

@Builder
public record GoogleUserData(
        String name,
        String email,
        String oidcSubject
) {}
