package co.com.pragma.model.gateway;

import reactor.core.publisher.Mono;

public interface ReportSQSGateway {
  Mono<Void> emit(String mensaje);
}