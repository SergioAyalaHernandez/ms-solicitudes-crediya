package co.com.pragma.model.gateway;

import java.util.List;

public interface JwtProvider {
  boolean validateToken(String token);
  String getUserIdFromToken(String token);
  List<String> getRoleFromToken(String token);
}
