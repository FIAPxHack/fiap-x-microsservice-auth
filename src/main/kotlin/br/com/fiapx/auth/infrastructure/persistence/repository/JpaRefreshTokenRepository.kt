package br.com.fiapx.auth.infrastructure.persistence.repository

import br.com.fiapx.auth.infrastructure.persistence.entity.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaRefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByToken(token: String): RefreshTokenEntity?
    fun findByUserId(userId: UUID): List<RefreshTokenEntity>
    
    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.userId = :userId")
    fun revokeAllByUserId(userId: UUID)
}
