package co.com.pragma.model.gateway;

import reactor.core.publisher.Mono;

public interface NotificacionSQSCapacidadGateway {
  Mono<Void> emit(String mensaje);
}
