package co.com.pragma.model.gateway;

import reactor.core.publisher.Mono;

public interface NotificacionSQSGateway {

  Mono<Void> emit(String mensaje);
}

