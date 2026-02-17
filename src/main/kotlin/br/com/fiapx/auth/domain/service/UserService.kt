package br.com.fiapx.auth.domain.service

import br.com.fiapx.auth.domain.model.User
import java.util.UUID

interface UserService {
    /**
     * Finds a user by email.
     * @param email The email to search for
     * @return The user if found
     * @throws UserNotFoundException if user is not found
     * @throws UserServiceException if there's an error communicating with User Service
     */
    fun findByEmail(email: String): User
    
    /**
     * Finds a user by ID.
     * @param id The user ID
     * @return The user if found
     * @throws UserNotFoundException if user is not found
     * @throws UserServiceException if there's an error communicating with User Service
     */
    fun findById(id: UUID): User
}
