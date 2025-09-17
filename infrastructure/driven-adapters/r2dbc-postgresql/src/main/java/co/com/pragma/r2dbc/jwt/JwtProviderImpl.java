package co.com.pragma.r2dbc.jwt;

import co.com.pragma.model.gateway.JwtProvider;
import co.com.pragma.r2dbc.utils.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Log
public class JwtProviderImpl implements JwtProvider {

  private final Key secretKey;

  public JwtProviderImpl(@Value("${jwt.secret}") String jwtSecret) {
    this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    log.info(Constants.JWT_SECRET_LOADED);
  }

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
      log.warning(Constants.ERROR_PROCESSING_TOKEN + e.getMessage());
      throw new RuntimeException(Constants.INVALID_TOKEN + e.getMessage(), e);
    }
  }

  @Override
  public String getEmailFromToken(String token) {
    log.info(Constants.TOKEN_RECEIVED + token);
    try {
      if (token != null && token.startsWith(Constants.BEARER)) {
        token = token.substring(7);
      }
      token = token != null ? token.trim() : "";

      Claims claims = Jwts.parserBuilder()
              .setSigningKey(secretKey)
              .build()
              .parseClaimsJws(token)
              .getBody();
      String encodedEmail = claims.get(Constants.EMAIL, String.class);
      return new String(Base64.getDecoder().decode(encodedEmail));
    } catch (JwtException | IllegalArgumentException e) {
      log.warning(Constants.ERROR_GETTING_EMAIL + e.getMessage());
      throw new RuntimeException(Constants.INVALID_TOKEN + e.getMessage(), e);
    }
  }

  @Override
  public Double getSalarioFromToken(String token) {
    try {
      if (token != null && token.startsWith(Constants.BEARER)) {
        token = token.substring(7);
      }
      token = token != null ? token.trim() : "";
      Claims claims = Jwts.parserBuilder()
              .setSigningKey(secretKey)
              .build()
              .parseClaimsJws(token)
              .getBody();
      return claims.get("salarioBase", Double.class);
    } catch (JwtException | IllegalArgumentException e) {
      log.warning(Constants.ERROR_GETTING_SALARY + e.getMessage());
      throw new RuntimeException(Constants.INVALID_TOKEN + e.getMessage(), e);
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
      log.info(Constants.ROLES_EXTRACTED + (rolesStr != null ? rolesStr : "null"));

      if (rolesStr == null || rolesStr.isEmpty()) {
          log.info(Constants.NO_ROLES_FOUND);
        return List.of();
      }

      List<String> roles = Arrays.stream(rolesStr.split(","))
              .map(String::trim)
              .collect(Collectors.toList());

      log.info(Constants.ROLES_PROCESSED + roles);
      return roles;
    } catch (Exception e) {
      log.info(Constants.ERROR_GETTING_ROLES + e.getMessage());
      return List.of();
    }
  }
}