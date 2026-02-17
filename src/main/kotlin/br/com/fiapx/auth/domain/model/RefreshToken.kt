package br.com.fiapx.auth.domain.model

import java.time.Instant
import java.util.UUID

data class RefreshToken(
    val id: UUID,
    val userId: UUID,
    val token: String,
    val expiresAt: Instant,
    val revoked: Boolean,
    val createdAt: Instant
)
