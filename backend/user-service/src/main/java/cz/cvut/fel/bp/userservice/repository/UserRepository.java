package cz.cvut.fel.bp.userservice.repository;

import cz.cvut.fel.bp.userservice.model.User;
import feign.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity.
 * Provides methods to perform CRUD operations and custom queries on the user data.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByOidcSubject(String oidcSubject);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Slice<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByOidcSubject(String subject);
    boolean existsByUsernameAndIdNot(String username, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByOidcSubjectAndIdNot(String oidcSubject, UUID id);
    @Query("SELECT u.id FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<UUID> findUserIdsByUsernameKeyword(@Param("keyword") String keyword);
}
