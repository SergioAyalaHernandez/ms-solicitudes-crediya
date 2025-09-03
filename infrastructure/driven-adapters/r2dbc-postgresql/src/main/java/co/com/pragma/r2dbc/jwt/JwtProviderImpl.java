package co.com.pragma.r2dbc.jwt;

import co.com.pragma.model.gateway.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Log
public class JwtProviderImpl implements JwtProvider {

  private final Key secretKey = Keys.hmacShaKeyFor("ClaveSuperSecretaDeJWTQueDebeTenerAlMenos256Bits!".getBytes());

  @Override
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
              .setSigningKey(secretKey)
              .build()
              .parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
  @Override
  public String getUserIdFromToken(String token) {
    try {
      if (token != null && token.startsWith("Bearer ")) {
        token = token.substring(7);
      }
      token = token != null ? token.trim() : "";

      Claims claims = Jwts.parserBuilder()
              .setSigningKey(secretKey)
              .build()
              .parseClaimsJws(token)
              .getBody();
      String objectId = claims.get("objectId", String.class);
      return objectId != null ? objectId : claims.getSubject();
    } catch (JwtException | IllegalArgumentException e) {
      log.warning("Error al procesar el token JWT: " + e.getMessage());
      throw new RuntimeException("Token inválido: " + e.getMessage(), e);
    }
  }

  @Override
  public List<String> getRoleFromToken(String token) {
    try {
      Claims claims = Jwts.parserBuilder()
              .setSigningKey(secretKey)
              .build()
              .parseClaimsJws(token)
              .getBody();

      String rolesStr = claims.get("roles", String.class);
      log.info("Roles extraídos del token: " + (rolesStr != null ? rolesStr : "null"));

      if (rolesStr == null || rolesStr.isEmpty()) {
        log.info("No se encontraron roles en el token");
        return List.of();
      }

      List<String> roles = Arrays.stream(rolesStr.split(","))
              .map(String::trim)
              .collect(Collectors.toList());

      log.info("Roles procesados: " + roles);
      return roles;
    } catch (Exception e) {
      log.info("Error al obtener roles del token: " + e.getMessage());
      return List.of();
    }
  }
}