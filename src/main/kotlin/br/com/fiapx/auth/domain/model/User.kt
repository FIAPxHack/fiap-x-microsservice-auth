package br.com.fiapx.auth.domain.model

import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val role: String
)
