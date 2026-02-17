package br.com.fiapx.auth.infrastructure.persistence.mapper

import br.com.fiapx.auth.domain.model.RefreshToken
import br.com.fiapx.auth.infrastructure.persistence.entity.RefreshTokenEntity

object RefreshTokenMapper {
    
    fun toDomain(entity: RefreshTokenEntity): RefreshToken {
        return RefreshToken(
            id = entity.id,
            userId = entity.userId,
            token = entity.token,
            expiresAt = entity.expiresAt,
            revoked = entity.revoked,
            createdAt = entity.createdAt
        )
    }
    
    fun toEntity(domain: RefreshToken): RefreshTokenEntity {
        return RefreshTokenEntity(
            id = domain.id,
            userId = domain.userId,
            token = domain.token,
            expiresAt = domain.expiresAt,
            revoked = domain.revoked,
            createdAt = domain.createdAt
        )
    }
}
