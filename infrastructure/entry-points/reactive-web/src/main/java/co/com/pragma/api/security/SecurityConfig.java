package co.com.pragma.api.security;

import co.com.pragma.api.utils.Constants;
import co.com.pragma.model.gateway.JwtProvider;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
@AllArgsConstructor
@Log
public class SecurityConfig {
  private final JwtProvider jwtProvider;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                    .pathMatchers(HttpMethod.POST, Constants.LOGIN_PATH).permitAll()
                    .pathMatchers(HttpMethod.POST, Constants.USERS_PATH).hasAnyRole(Constants.ROLE_ADMIN, Constants.ROLE_ADVISOR)
                    .pathMatchers(HttpMethod.GET, Constants.USERS_PATH_WILDCARD).hasAnyRole(Constants.ROLE_ADMIN, Constants.ROLE_USER, Constants.ROLE_CLIENT)
                    .pathMatchers(HttpMethod.POST, Constants.REQUESTS_PATH_WILDCARD).hasRole(Constants.ROLE_CLIENT)
                    .pathMatchers(HttpMethod.POST, Constants.SOLICITUD_PATH).authenticated()
                    .pathMatchers(HttpMethod.GET, Constants.SOLICITUDES_PATH).hasRole(Constants.ROLE_ADVISOR)
                    .pathMatchers(Constants.WEBJARS_PATH).permitAll()
                    .pathMatchers(Constants.SWAGGER_UI_PATH).permitAll()
                    .pathMatchers(Constants.SWAGGER_RESOURCES_PATH).permitAll()
                    .pathMatchers(Constants.API_DOCS_PATHS).permitAll()
                    .anyExchange().authenticated()
            )
            .addFilterAt(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
  }

  @Bean
  public AuthenticationWebFilter jwtAuthenticationFilter() {
    AuthenticationWebFilter filter = new AuthenticationWebFilter(reactiveAuthenticationManager());
    filter.setServerAuthenticationConverter(this::convert);
    return filter;
  }

  private Mono<Authentication> convert(ServerWebExchange exchange) {
    String authHeader = exchange.getRequest().getHeaders().getFirst(Constants.AUTHORIZATION_HEADER);
    if (authHeader != null && authHeader.startsWith(Constants.BEARER_PREFIX)) {
      String token = authHeader.substring(Constants.BEARER_PREFIX.length());
      if (jwtProvider.validateToken(token)) {
        String userId = jwtProvider.getUserIdFromToken(token);
        List<String> roles = jwtProvider.getRoleFromToken(token);
        var authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(Constants.ROLE_PREFIX + role.trim()))
                .collect(Collectors.toList());
        var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        return Mono.just(authentication);
      } else {
        log.info(Constants.TOKEN_ERROR);
      }
    }
    return Mono.empty();
  }

  @Bean
  public ReactiveAuthenticationManager reactiveAuthenticationManager() {
    return Mono::just;
  }
}
