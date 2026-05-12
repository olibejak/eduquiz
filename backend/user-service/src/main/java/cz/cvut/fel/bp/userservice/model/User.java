package cz.cvut.fel.bp.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User entity representing a user in the system.
 */
@Entity
@Table(name = "quiz_user")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(unique = true, nullable = false, name = "oidc_subject")
    private String oidcSubject;

    /*
    Info: OAuth2 will be used
    Todo: Implement own security
    @Column(nullable = false)
    private String password;
     */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // Info: Delete user history when deleting the user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserQuizResult> quizHistories = new ArrayList<>();
}
