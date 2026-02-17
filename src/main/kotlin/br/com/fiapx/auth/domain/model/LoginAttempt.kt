package br.com.fiapx.auth.domain.model

import java.time.Instant
import java.util.UUID

data class LoginAttempt(
    val id: UUID,
    val email: String?,
    val success: Boolean,
    val ipAddress: String?,
    val createdAt: Instant
)
