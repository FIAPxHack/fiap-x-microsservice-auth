package br.com.fiapx.auth.infrastructure.persistence.mapper

import br.com.fiapx.auth.domain.model.LoginAttempt
import br.com.fiapx.auth.infrastructure.persistence.entity.LoginAttemptEntity

object LoginAttemptMapper {
    
    fun toDomain(entity: LoginAttemptEntity): LoginAttempt {
        return LoginAttempt(
            id = entity.id,
            email = entity.email,
            success = entity.success,
            ipAddress = entity.ipAddress,
            createdAt = entity.createdAt
        )
    }
    
    fun toEntity(domain: LoginAttempt): LoginAttemptEntity {
        return LoginAttemptEntity(
            id = domain.id,
            email = domain.email,
            success = domain.success,
            ipAddress = domain.ipAddress,
            createdAt = domain.createdAt
        )
    }
}
