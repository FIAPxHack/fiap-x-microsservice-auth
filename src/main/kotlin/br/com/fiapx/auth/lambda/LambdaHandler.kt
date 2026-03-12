package br.com.fiapx.auth.lambda

import br.com.fiapx.auth.AuthServiceApplication
import com.amazonaws.serverless.exceptions.ContainerInitializationException
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.slf4j.LoggerFactory

class LambdaHandler : RequestHandler<AwsProxyRequest, AwsProxyResponse> {

    override fun handleRequest(input: AwsProxyRequest, context: Context): AwsProxyResponse {
        return handler.proxy(input, context)
    }

    companion object {
        private val log = LoggerFactory.getLogger(LambdaHandler::class.java)

        private val handler: SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> =
            try {
                SpringBootLambdaContainerHandler.getAwsProxyHandler(AuthServiceApplication::class.java)
            } catch (e: ContainerInitializationException) {
                log.error("Não foi possível inicializar o contexto Spring para AWS Lambda", e)
                throw RuntimeException("Falha ao inicializar aplicação para AWS Lambda", e)
            }
    }
}
