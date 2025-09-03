package co.com.pragma.model.gateway;

import reactor.core.publisher.Mono;

public interface CreditTypeGateway {
  Mono<Long> getCreditTypeById(Long id);
}
