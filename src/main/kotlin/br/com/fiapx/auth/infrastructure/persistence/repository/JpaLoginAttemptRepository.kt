package br.com.fiapx.auth.infrastructure.persistence.repository

import br.com.fiapx.auth.infrastructure.persistence.entity.LoginAttemptEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaLoginAttemptRepository : JpaRepository<LoginAttemptEntity, UUID> {
    fun findByEmail(email: String): List<LoginAttemptEntity>
}
