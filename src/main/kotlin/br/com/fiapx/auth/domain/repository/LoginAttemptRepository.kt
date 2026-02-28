package br.com.fiapx.auth.domain.repository

import br.com.fiapx.auth.domain.model.LoginAttempt

interface LoginAttemptRepository {
    fun save(loginAttempt: LoginAttempt): LoginAttempt
    fun findByEmail(email: String): List<LoginAttempt>
}
