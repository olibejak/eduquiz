package cz.cvut.fel.bp.deckservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a Deck entity.
 * Each Deck has a title, description, author, and a set of tags.
 * The createdAt and updatedAt fields are automatically managed.
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deck_gen")
    @SequenceGenerator(name = "deck_gen", sequenceName = "deck_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    // Author ID is stored in user-service DB
    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisibilityType visibility;

    // Using ElementCollection to store a set of DeckTag enums in a separate table
    @ElementCollection(targetClass = DeckTagType.class)
    @CollectionTable(
            name = "deck_tag", // Helper table
            joinColumns = @JoinColumn(name = "deck_id") // Foreign key
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false)
    @Builder.Default // Ensure HashSet works when using Builder
    private Set<DeckTagType> tags = new HashSet<>();

    // One-to-many relationship with Question
    // Warn: Use List for ensuring the order and avoiding issues with equals/hashCode in Sets when using JPA
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    // Automatically managed create timestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Automatically managed update timestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Using ElementCollection to store a set of user IDs, who added this deck to favorites, in a separate table
    @ElementCollection
    @CollectionTable(
            name = "favorite_deck", // Table name
            joinColumns = @JoinColumn(name = "deck_id") // Foreign key
    )
    @Column(name = "user_id", nullable = false)
    @Builder.Default
    private Set<UUID> favoritedByUsers = new HashSet<>();

    /**
     * Called before the entity is persisted.
     * Sets the createdAt and updatedAt timestamps to the current time.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Called before the entity is updated.
     * Updates the updatedAt timestamp to the current time.
     */
    //Todo: exclude favorites update from onUpdate
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to add a question to the deck and set the bidirectional relationship.
     * @param question The question to be added to the deck.
     */
    public void addQuestion(Question question) {
        questions.add(question);
        question.setDeck(this);
    }

    /**
     * Helper method to remove a question from the deck and clear the bidirectional relationship.
     * @param question The question to be removed from the deck.
     */
    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setDeck(null);
    }

    /**
     * Helper method to add a user ID to the set of users who favorited this deck.
     * @param userId The ID of the user who added this deck to favorites.
     */
    public void addFavorite(UUID userId) {
        favoritedByUsers.add(userId);
    }

    /**
     * Helper method to remove a user ID from the set of users who favorited this deck.
     * @param userId The ID of the user who removed this deck from favorites.
     */
    public void removeFavorite(UUID userId) {
        favoritedByUsers.remove(userId);
    }

    /**
     * Helper method to add a tag to the set of tags.
     * @param tag set of tags to be added
     */
    public void addTag(DeckTagType tag) {
        tags.add(tag);
    }

    /**
     * Helper method to add multiple tags to the set of tags.
     * @param tags set of tags to be added
     */
    public void addTags(Set<DeckTagType> tags) {
        this.tags.addAll(tags);
    }

    /**
     * Helper method to remove a tag from the set of tags.
     * @param tag set of tags to be added
     */
    public void removeTag(DeckTagType tag) {
        tags.remove(tag);
    }

    /**
     * Helper method to clear the set of tags.
     */
    public void clearTags() {
        tags.clear();
    }
}
