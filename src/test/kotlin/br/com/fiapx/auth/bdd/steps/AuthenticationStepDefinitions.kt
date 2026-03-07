package br.com.fiapx.auth.bdd.steps

import br.com.fiapx.auth.bdd.config.TestMockBeans
import br.com.fiapx.auth.domain.model.User
import br.com.fiapx.auth.domain.service.UserService
import io.cucumber.java.pt.Dado
import io.cucumber.java.pt.Quando
import io.cucumber.java.pt.Entao
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID

@Import(TestMockBeans::class)
class AuthenticationStepDefinitions {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var userService: UserService

    private var responseStatusCode: Int = 0
    private var responseBody: String = ""
    private val passwordEncoder = BCryptPasswordEncoder()
    private val httpClient = HttpClient.newHttpClient()

    @Dado("que existe um usuario cadastrado com email {string} e senha {string}")
    fun queExisteUmUsuarioCadastradoComEmailESenha(email: String, senha: String) {
        val rawPassword: String = senha
        val hashedPassword: String = passwordEncoder.encode(rawPassword)!!
        val testUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")

        every { userService.findByEmail(email) } returns User(
            id = testUserId,
            email = email,
            passwordHash = hashedPassword,
            role = "USER"
        )
    }

    @Quando("eu envio uma requisicao de autenticacao com email {string} e senha {string}")
    fun euEnvioUmaRequisicaoDeAutenticacaoComEmailESenha(email: String, senha: String) {
        val body = """{"email": "$email", "password": "$senha"}"""

        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:$port/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        responseStatusCode = response.statusCode()
        responseBody = response.body() ?: ""
    }

    @Entao("o status da resposta deve ser {int}")
    fun oStatusDaRespostaDeveSer(statusCode: Int) {
        assert(responseStatusCode == statusCode) {
            "Status esperado: $statusCode, recebido: $responseStatusCode"
        }
    }

    @Entao("a resposta deve conter um token JWT valido")
    fun aRespostaDeveConterUmTokenJwtValido() {
        assert(responseBody.contains("accessToken")) { "A resposta deveria conter accessToken" }
        assert(responseBody.contains("refreshToken")) { "A resposta deveria conter refreshToken" }
        assert(responseBody.contains("Bearer")) { "A resposta deveria conter tokenType Bearer" }
    }

    @Entao("a resposta deve conter a mensagem {string}")
    fun aRespostaDeveConterAMensagem(mensagem: String) {
        assert(responseBody.contains(mensagem) || responseBody.lowercase().contains(mensagem.lowercase())) {
            "A resposta deveria conter a mensagem '$mensagem', mas continha: $responseBody"
        }
    }
}
