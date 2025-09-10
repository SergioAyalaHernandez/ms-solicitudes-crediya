package co.com.pragma.model.gateway;

import java.util.List;

public interface JwtProvider {
  boolean validateToken(String token);
  String getUserIdFromToken(String token);
  String getEmailFromToken(String token);
  Double getSalarioFromToken(String token);
  List<String> getRoleFromToken(String token);
}
