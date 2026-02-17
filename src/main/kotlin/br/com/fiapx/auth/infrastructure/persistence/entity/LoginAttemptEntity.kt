package br.com.fiapx.auth.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "login_attempts")
class LoginAttemptEntity(
    
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "email", length = 255)
    val email: String?,
    
    @Column(name = "success", nullable = false)
    val success: Boolean,
    
    @Column(name = "ip_address", length = 100)
    val ipAddress: String?,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
