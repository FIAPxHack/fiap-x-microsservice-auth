package br.com.fiapx.auth.infrastructure.security

import br.com.fiapx.auth.domain.service.PasswordService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class PasswordServiceImpl : PasswordService {
    
    private val encoder = BCryptPasswordEncoder()
    
    override fun matches(rawPassword: String, hashedPassword: String): Boolean {
        return encoder.matches(rawPassword, hashedPassword)
    }
    
    override fun hash(rawPassword: String): String {
        return encoder.encode(rawPassword)!!
    }
}
