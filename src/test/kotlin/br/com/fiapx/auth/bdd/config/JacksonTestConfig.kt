package br.com.fiapx.auth.bdd.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class JacksonTestConfig {

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
}

