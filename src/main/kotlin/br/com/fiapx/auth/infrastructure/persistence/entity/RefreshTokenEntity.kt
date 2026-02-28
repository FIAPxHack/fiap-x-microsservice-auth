package br.com.fiapx.auth.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    val userId: UUID,
    
    @Column(name = "token", nullable = false, length = 500)
    val token: String,
    
    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,
    
    @Column(name = "revoked", nullable = false)
    val revoked: Boolean = false,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
