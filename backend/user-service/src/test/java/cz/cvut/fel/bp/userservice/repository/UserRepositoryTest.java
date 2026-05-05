package cz.cvut.fel.bp.userservice.repository;

import cz.cvut.fel.bp.userservice.model.User;
import cz.cvut.fel.bp.userservice.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserIdsByUsernameKeywordIgnoringCase() {
        User john = User.builder()
                .username("JohnDoe")
                .email("john@example.com")
                .oidcSubject("sub-john")
                .role(UserRole.USER)
                .build();
        User alice = User.builder()
                .username("alice")
                .email("alice@example.com")
                .oidcSubject("sub-alice")
                .role(UserRole.USER)
                .build();

        userRepository.saveAll(List.of(john, alice));

        List<UUID> ids = userRepository.findUserIdsByUsernameKeyword("doe");

        assertEquals(1, ids.size());
        assertEquals(john.getId(), ids.getFirst());
    }

    @Test
    void shouldCheckEmailExistsForAnotherUser() {
        User first = userRepository.save(User.builder()
                .username("first")
                .email("same@example.com")
                .oidcSubject("sub-1")
                .role(UserRole.USER)
                .build());
        User second = userRepository.save(User.builder()
                .username("second")
                .email("second@example.com")
                .oidcSubject("sub-2")
                .role(UserRole.USER)
                .build());

        boolean exists = userRepository.existsByEmailAndIdNot("same@example.com", second.getId());
        boolean notExistsForSelf = userRepository.existsByEmailAndIdNot("same@example.com", first.getId());

        assertTrue(exists);
        assertFalse(notExistsForSelf);
    }

    @Test
    void shouldReturnSliceByUsernameContaining() {
        userRepository.save(User.builder().username("alpha").email("a@example.com").oidcSubject("sub-a").role(UserRole.USER).build());
        userRepository.save(User.builder().username("alphabet").email("b@example.com").oidcSubject("sub-b").role(UserRole.USER).build());
        userRepository.save(User.builder().username("gamma").email("c@example.com").oidcSubject("sub-c").role(UserRole.USER).build());

        var slice = userRepository.findByUsernameContainingIgnoreCase("alp", PageRequest.of(0, 10));

        assertEquals(2, slice.getNumberOfElements());
    }
}

