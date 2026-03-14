package br.com.fiapx.auth.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsConfig {

    @Bean
    fun authMetrics(meterRegistry: MeterRegistry): AuthMetrics = AuthMetrics(meterRegistry)
}

class AuthMetrics(
    meterRegistry: MeterRegistry
) {
    val loginSuccessCounter: Counter = Counter.builder("auth_login_attempts_total")
        .description("Total de tentativas de login com sucesso")
        .tag("outcome", "success")
        .register(meterRegistry)

    val loginFailureCounter: Counter = Counter.builder("auth_login_attempts_total")
        .description("Total de tentativas de login com falha")
        .tag("outcome", "failure")
        .register(meterRegistry)

    val loginTimer: Timer = Timer.builder("auth_login_duration")
        .description("Tempo de execução do caso de uso de login")
        .register(meterRegistry)

    val refreshSuccessCounter: Counter = Counter.builder("auth_refresh_token_requests_total")
        .description("Total de renovações de token com sucesso")
        .tag("outcome", "success")
        .register(meterRegistry)

    val refreshFailureCounter: Counter = Counter.builder("auth_refresh_token_requests_total")
        .description("Total de renovações de token com falha")
        .tag("outcome", "failure")
        .register(meterRegistry)

    val refreshTimer: Timer = Timer.builder("auth_refresh_token_duration")
        .description("Tempo de execução do caso de uso de refresh token")
        .register(meterRegistry)

    val validateSuccessCounter: Counter = Counter.builder("auth_validate_token_requests_total")
        .description("Total de validações de token com sucesso")
        .tag("outcome", "success")
        .register(meterRegistry)

    val validateFailureCounter: Counter = Counter.builder("auth_validate_token_requests_total")
        .description("Total de validações de token com falha")
        .tag("outcome", "failure")
        .register(meterRegistry)

    val validateTimer: Timer = Timer.builder("auth_validate_token_duration")
        .description("Tempo de execução do caso de uso de validação de token")
        .register(meterRegistry)
}

