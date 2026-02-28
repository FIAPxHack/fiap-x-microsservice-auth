package br.com.fiapx.auth.infrastructure.persistence.repository

import br.com.fiapx.auth.domain.model.LoginAttempt
import br.com.fiapx.auth.domain.repository.LoginAttemptRepository
import br.com.fiapx.auth.infrastructure.persistence.mapper.LoginAttemptMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LoginAttemptRepositoryImpl(
    private val jpaRepository: JpaLoginAttemptRepository
) : LoginAttemptRepository {
    
    @Transactional
    override fun save(loginAttempt: LoginAttempt): LoginAttempt {
        val entity = LoginAttemptMapper.toEntity(loginAttempt)
        val saved = jpaRepository.save(entity)
        return LoginAttemptMapper.toDomain(saved)
    }
    
    override fun findByEmail(email: String): List<LoginAttempt> {
        return jpaRepository.findByEmail(email).map { LoginAttemptMapper.toDomain(it) }
    }
}
