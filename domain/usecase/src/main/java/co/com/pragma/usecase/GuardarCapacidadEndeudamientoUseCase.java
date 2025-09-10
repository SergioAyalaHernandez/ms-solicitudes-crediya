package co.com.pragma.usecase;

import co.com.pragma.model.credit.MessageCapacidadEndeudamiento;
import co.com.pragma.model.gateway.CreditGateway;
import co.com.pragma.usecase.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

@Log
@AllArgsConstructor
public class GuardarCapacidadEndeudamientoUseCase {
  private final CreditGateway creditGateway;

  public Mono<Void> actualizarEstadoSolicitudCredito(MessageCapacidadEndeudamiento message) {
    return creditGateway.updateState(message.getIdSolicitud(), message.getEstado())
            .doOnSuccess(v -> log.info(Constants.LOG_SUCCESS))
            .doOnError(e -> log.severe(Constants.LOG_ERROR + e.getMessage()));
  }
}