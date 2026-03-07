package br.com.fiapx.auth.bdd.config

import br.com.fiapx.auth.domain.service.PasswordService
import br.com.fiapx.auth.domain.service.UserService
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestMockBeans {

    @Bean
    @Primary
    fun testUserService(): UserService = mockk(relaxed = true)

    @Bean
    @Primary
    fun testPasswordService(): PasswordService = mockk(relaxed = true)
}
