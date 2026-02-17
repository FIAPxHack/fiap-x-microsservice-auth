package br.com.fiapx.auth.infrastructure.persistence.repository

import br.com.fiapx.auth.domain.model.RefreshToken
import br.com.fiapx.auth.domain.repository.RefreshTokenRepository
import br.com.fiapx.auth.infrastructure.persistence.mapper.RefreshTokenMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class RefreshTokenRepositoryImpl(
    private val jpaRepository: JpaRefreshTokenRepository
) : RefreshTokenRepository {
    
    @Transactional
    override fun save(refreshToken: RefreshToken): RefreshToken {
        val entity = RefreshTokenMapper.toEntity(refreshToken)
        val saved = jpaRepository.save(entity)
        return RefreshTokenMapper.toDomain(saved)
    }
    
    override fun findByToken(token: String): RefreshToken? {
        return jpaRepository.findByToken(token)?.let { RefreshTokenMapper.toDomain(it) }
    }
    
    override fun findByUserId(userId: UUID): List<RefreshToken> {
        return jpaRepository.findByUserId(userId).map { RefreshTokenMapper.toDomain(it) }
    }
    
    @Transactional
    override fun revokeAllByUserId(userId: UUID) {
        jpaRepository.revokeAllByUserId(userId)
    }
    
    @Transactional
    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }
}
