package br.com.fiapx.auth.domain.repository

import br.com.fiapx.auth.domain.model.RefreshToken
import java.util.UUID

interface RefreshTokenRepository {
    fun save(refreshToken: RefreshToken): RefreshToken
    fun findByToken(token: String): RefreshToken?
    fun findByUserId(userId: UUID): List<RefreshToken>
    fun revokeAllByUserId(userId: UUID)
    fun deleteById(id: UUID)
}
