package br.com.fiapx.auth.domain.exception


abstract class AuthException(message: String) : RuntimeException(message)

class InvalidCredentialsException(message: String = "Credenciais inválidas") : AuthException(message)
class TokenExpiredException(message: String = "Token expirou") : AuthException(message)
class InvalidTokenException(message: String = "Token inválido") : AuthException(message)
class TokenRevokedException(message: String = "O token foi revogado") : AuthException(message)
class UserNotFoundException(message: String = "Usuário não encontrado") : AuthException(message)
class UserServiceException(message: String) : AuthException(message)
