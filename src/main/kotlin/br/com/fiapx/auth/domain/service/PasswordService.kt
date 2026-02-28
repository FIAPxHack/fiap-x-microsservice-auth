package br.com.fiapx.auth.domain.service

/**
 * Service interface for password operations.
 * This is a pure domain interface with no framework dependencies.
 */
interface PasswordService {
    /**
     * Validates a raw password against a hashed password.
     * @param rawPassword The raw password to validate
     * @param hashedPassword The hashed password to compare against
     * @return true if passwords match, false otherwise
     */
    fun matches(rawPassword: String, hashedPassword: String): Boolean
    
    /**
     * Hashes a raw password.
     * @param rawPassword The raw password to hash
     * @return The hashed password
     */
    fun hash(rawPassword: String): String
}
