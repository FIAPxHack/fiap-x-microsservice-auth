package br.com.fiapx.auth.infrastructure.jwt

import br.com.fiapx.auth.domain.exception.InvalidTokenException
import br.com.fiapx.auth.domain.exception.TokenExpiredException
import br.com.fiapx.auth.domain.service.JwtService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

class JwtServiceImpl(
    private val secret: String,
    private val accessTokenExpirationMs: Long,
    private val issuer: String
) : JwtService {
    
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())
    
    override fun generateAccessToken(userId: UUID, email: String, role: String): String {
        val now = Date()
        val expiryDate = Date(now.time + accessTokenExpirationMs)
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .issuer(issuer)
            .signWith(secretKey)
            .compact()
    }
    
    override fun generateRefreshToken(): String {
        return UUID.randomUUID().toString()
    }
    
    override fun validateToken(token: String): Map<String, Any> {
        try {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
            
            return mapOf(
                "sub" to claims.subject,
                "email" to claims["email"]!!,
                "role" to claims["role"]!!,
                "iat" to claims.issuedAt,
                "exp" to claims.expiration
            )
        } catch (e: ExpiredJwtException) {
            throw TokenExpiredException("Token expirou")
        } catch (e: Exception) {
            throw InvalidTokenException("Token inválido: ${e.message}")
        }
    }
    
    override fun extractUserId(token: String): UUID {
        val claims = extractAllClaims(token)
        return UUID.fromString(claims.subject)
    }
    
    override fun extractEmail(token: String): String {
        val claims = extractAllClaims(token)
        return claims["email"] as String
    }
    
    override fun extractRole(token: String): String {
        val claims = extractAllClaims(token)
        return claims["role"] as String
    }
    
    private fun extractAllClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            e.claims
        } catch (e: Exception) {
            throw InvalidTokenException("Não é possível extrair reivindicações do token: ${e.message}")
        }
    }
}
