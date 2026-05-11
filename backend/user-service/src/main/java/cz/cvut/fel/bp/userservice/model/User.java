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
    private List<UserQuizHistory> quizHistories = new ArrayList<>();

    /**
     * Helper method to add quiz history to the user and set the user reference in the history.
     * @param history the quiz history to add
     */
    public void addQuizHistory(UserQuizHistory history) {
        quizHistories.add(history);
        history.setUser(this);
    }

    /**
     * Helper method to remove quiz history from the user and clear the user reference in the history.
     * @param history the quiz history to remove
     */
    public void removeQuizHistory(UserQuizHistory history) {
        quizHistories.remove(history);
        history.setUser(null);
    }
}
