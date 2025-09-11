package co.com.pragma.r2dbc.jwt;

import co.com.pragma.model.gateway.JwtProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderImplTest {

  private JwtProvider jwtProvider;
  private final Key testSecretKey = Keys.hmacShaKeyFor("ClaveSuperSecretaDeJWTQueDebeTenerAlMenos256Bits!".getBytes());

  @BeforeEach
  void setUp() {
    jwtProvider = new JwtProviderImpl();
  }

  private String generateValidToken(String userId, String roles) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
            .setSubject(userId)
            .claim("objectId", userId)
            .claim("roles", roles)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + 3600000)) // 1 hora
            .signWith(testSecretKey, SignatureAlgorithm.HS256)
            .compact();
  }

  private String generateInvalidToken() {
    long now = System.currentTimeMillis();
    Key wrongKey = Keys.hmacShaKeyFor("OtraClaveDistintaParaGenerarTokenInvalido12345!".getBytes());
    return Jwts.builder()
            .setSubject("user123")
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + 3600000))
            .signWith(wrongKey, SignatureAlgorithm.HS256)
            .compact();
  }

  @Test
  @DisplayName("Debería validar un token correcto")
  void validateToken_withValidToken_shouldReturnTrue() {
    // Arrange
    String validToken = generateValidToken("user123", "ROLE_USER,ROLE_ADMIN");

    // Act
    boolean result = jwtProvider.validateToken(validToken);

    // Assert
    assertTrue(result);
  }

  @Test
  @DisplayName("Debería rechazar un token inválido")
  void validateToken_withInvalidToken_shouldReturnFalse() {
    // Arrange
    String invalidToken = generateInvalidToken();

    // Act
    boolean result = jwtProvider.validateToken(invalidToken);

    // Assert
    assertFalse(result);
  }

  @Test
  @DisplayName("Debería rechazar un token malformado")
  void validateToken_withMalformedToken_shouldReturnFalse() {
    // Arrange
    String malformedToken = "esto.no.es.un.token.jwt";

    // Act
    boolean result = jwtProvider.validateToken(malformedToken);

    // Assert
    assertFalse(result);
  }

  @Test
  @DisplayName("Debería extraer el ID de usuario de un token válido")
  void getUserIdFromToken_withValidToken_shouldReturnUserId() {
    // Arrange
    String userId = "user123";
    String validToken = generateValidToken(userId, "ROLE_USER");

    // Act
    String extractedUserId = jwtProvider.getUserIdFromToken(validToken);

    // Assert
    assertEquals(userId, extractedUserId);
  }

  @Test
  @DisplayName("Debería quitar el prefijo Bearer y extraer el ID de usuario")
  void getUserIdFromToken_withBearerPrefix_shouldRemovePrefixAndReturnUserId() {
    // Arrange
    String userId = "user123";
    String validToken = generateValidToken(userId, "ROLE_USER");
    String tokenWithBearer = "Bearer " + validToken;

    // Act
    String extractedUserId = jwtProvider.getUserIdFromToken(tokenWithBearer);

    // Assert
    assertEquals(userId, extractedUserId);
  }

  @Test
  @DisplayName("Debería lanzar RuntimeException con un token inválido")
  void getUserIdFromToken_withInvalidToken_shouldThrowRuntimeException() {
    // Arrange
    String invalidToken = generateInvalidToken();

    // Act & Assert
    assertThrows(RuntimeException.class, () -> jwtProvider.getUserIdFromToken(invalidToken));
  }

  @Test
  @DisplayName("Debería extraer roles de un token válido")
  void getRoleFromToken_withValidToken_shouldReturnListOfRoles() {
    // Arrange
    String roles = "ROLE_USER,ROLE_ADMIN";
    String validToken = generateValidToken("user123", roles);

    // Act
    List<String> extractedRoles = jwtProvider.getRoleFromToken(validToken);

    // Assert
    assertEquals(2, extractedRoles.size());
    assertTrue(extractedRoles.contains("ROLE_USER"));
    assertTrue(extractedRoles.contains("ROLE_ADMIN"));
  }

  @Test
  @DisplayName("Debería devolver lista vacía cuando el token no tiene roles")
  void getRoleFromToken_withEmptyRoles_shouldReturnEmptyList() {
    // Arrange
    String validToken = generateValidToken("user123", "");

    // Act
    List<String> extractedRoles = jwtProvider.getRoleFromToken(validToken);

    // Assert
    assertTrue(extractedRoles.isEmpty());
  }

  @Test
  @DisplayName("Debería devolver lista vacía para tokens inválidos")
  void getRoleFromToken_withInvalidToken_shouldReturnEmptyList() {
    // Arrange
    String invalidToken = generateInvalidToken();

    // Act
    List<String> extractedRoles = jwtProvider.getRoleFromToken(invalidToken);

    // Assert
    assertTrue(extractedRoles.isEmpty());
  }

  @Test
  @DisplayName("Debería extraer el correo electrónico de un token válido")
  void getEmailFromToken_withValidToken_shouldReturnEmail() {
    // Arrange
    String email = "usuario@correo.com";
    String encodedEmail = Base64.getEncoder().encodeToString(email.getBytes());
    long now = System.currentTimeMillis();
    String token = Jwts.builder()
            .setSubject("user123")
            .claim("correoElectronico", encodedEmail)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + 3600000))
            .signWith(testSecretKey, SignatureAlgorithm.HS256)
            .compact();

    // Act
    String extractedEmail = jwtProvider.getEmailFromToken(token);

    // Assert
    assertEquals(email, extractedEmail);
  }

  @Test
  @DisplayName("Debería lanzar RuntimeException si el token de correo es inválido")
  void getEmailFromToken_withInvalidToken_shouldThrowRuntimeException() {
    String invalidToken = generateInvalidToken();
    assertThrows(RuntimeException.class, () -> jwtProvider.getEmailFromToken(invalidToken));
  }

  @Test
  @DisplayName("Debería extraer el salario base de un token válido")
  void getSalarioFromToken_withValidToken_shouldReturnSalario() {
    // Arrange
    Double salario = 12345.67;
    long now = System.currentTimeMillis();
    String token = Jwts.builder()
            .setSubject("user123")
            .claim("salarioBase", salario)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + 3600000))
            .signWith(testSecretKey, SignatureAlgorithm.HS256)
            .compact();

    // Act
    Double extractedSalario = jwtProvider.getSalarioFromToken(token);

    // Assert
    assertEquals(salario, extractedSalario);
  }

  @Test
  @DisplayName("Debería lanzar RuntimeException si el token de salario es inválido")
  void getSalarioFromToken_withInvalidToken_shouldThrowRuntimeException() {
    String invalidToken = generateInvalidToken();
    assertThrows(RuntimeException.class, () -> jwtProvider.getSalarioFromToken(invalidToken));
  }
}