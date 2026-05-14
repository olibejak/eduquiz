package cz.cvut.fel.bp.userservice.model;

import cz.cvut.fel.bp.userservice.exception.InvalidCredentialsException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

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

    @JsonValue
    public String toJson() {
        return name;
    }

    /**
     * Converts a string role (e.g., "ROLE_USER") to the corresponding UserRole enum value.
     * @param role String representation of the role, can be with or without "ROLE_" prefix
     * @return UserRole enum value corresponding to the input string
     */
    public static UserRole fromString(String role) {
        if (role == null)
            throw new InvalidCredentialsException("Role cannot be null");
        if (role.equalsIgnoreCase("ROLE_USER"))
            return USER;
        else if (role.equalsIgnoreCase("ROLE_ADMIN"))
            return ADMIN;
        else
            throw new InvalidCredentialsException("Invalid role");
    }

    /**
     * Lenient JSON creator that accepts values like "ROLE_ADMIN" or "ADMIN" (case-insensitive).
     * This is used by Jackson when deserializing enums from incoming JSON.
     */
    @JsonCreator
    public static UserRole forJson(String value) {
        if (value == null) {
            throw new InvalidCredentialsException("Role cannot be null");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new InvalidCredentialsException("Invalid role: " + value);
        }
    }
}
