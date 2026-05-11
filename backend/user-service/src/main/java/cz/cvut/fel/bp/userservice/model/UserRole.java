package cz.cvut.fel.bp.userservice.model;

/**
 * Enum for user's role.
 * Currently only User and Admin; might be extended in the future.
 */
public enum UserRole {

    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String name;

    UserRole(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Converts a string role (e.g., "ROLE_USER") to the corresponding UserRole enum value.
     * @param role String representation of the role, can be with or without "ROLE_" prefix
     * @return UserRole enum value corresponding to the input string
     */
    public static UserRole toRole(String role) {
        String prepRole = role.startsWith("ROLE_") ? role.substring(5) : role;
        return UserRole.valueOf(prepRole);
    }
}
