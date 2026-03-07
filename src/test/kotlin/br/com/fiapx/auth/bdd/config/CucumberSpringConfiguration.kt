package br.com.fiapx.auth.bdd.config

import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import

@CucumberContextConfiguration
@Import(JacksonTestConfig::class, TestMockBeans::class)
class CucumberSpringConfiguration : AbstractIntegrationTest() {

    @LocalServerPort
    protected var port: Int = 0

    protected fun getBaseUrl(): String = "http://localhost:$port"
}
