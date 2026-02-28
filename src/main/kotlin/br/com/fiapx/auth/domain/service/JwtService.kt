package br.com.fiapx.auth.domain.service

import java.util.UUID

/**
 * Service interface for JWT operations.
 * This is a pure domain interface with no framework dependencies.
 */
interface JwtService {
    /**
     * Generates an access token for a user.
     * @param userId The user's ID
     * @param email The user's email
     * @param role The user's role
     * @return The generated JWT access token
     */
    fun generateAccessToken(userId: UUID, email: String, role: String): String
    
    /**
     * Generates a refresh token.
     * @return A unique refresh token string
     */
    fun generateRefreshToken(): String
    
    /**
     * Validates a JWT token and extracts claims.
     * @param token The JWT token to validate
     * @return Map of claims if valid
     * @throws InvalidTokenException if token is invalid
     * @throws TokenExpiredException if token has expired
     */
    fun validateToken(token: String): Map<String, Any>
    
    /**
     * Extracts the user ID from a token without full validation.
     * @param token The JWT token
     * @return The user ID
     */
    fun extractUserId(token: String): UUID
    
    /**
     * Extracts the email from a token without full validation.
     * @param token The JWT token
     * @return The email
     */
    fun extractEmail(token: String): String
    
    /**
     * Extracts the role from a token without full validation.
     * @param token The JWT token
     * @return The role
     */
    fun extractRole(token: String): String
}
