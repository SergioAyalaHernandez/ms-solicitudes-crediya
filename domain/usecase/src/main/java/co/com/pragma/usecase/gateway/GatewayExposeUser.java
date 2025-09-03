package co.com.pragma.usecase.gateway;

import reactor.core.publisher.Mono;

public interface GatewayExposeUser {
  Mono<Long> findByDocument(Long document);
}
