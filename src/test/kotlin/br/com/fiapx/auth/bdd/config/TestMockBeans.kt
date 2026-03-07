package br.com.fiapx.auth.bdd.config

import br.com.fiapx.auth.domain.model.User
import br.com.fiapx.auth.domain.service.UserService
import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.UUID

@TestConfiguration
class TestMockBeans {

    private val passwordEncoder = BCryptPasswordEncoder()

    @Bean
    @Primary
    fun mockUserService(): UserService {
        val mock = mockk<UserService>(relaxed = true)

        val testUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val rawPassword = "Senha@123"
        val hashedPassword: String = passwordEncoder.encode(rawPassword)!!

        every { mock.findByEmail("usuario@fiap.com") } returns User(
            id = testUserId,
            email = "usuario@fiap.com",
            passwordHash = hashedPassword,
            role = "USER"
        )

        return mock
    }
}
