package co.com.pragma.usecase;

import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.model.credit.MessageCapacidadEndeudamiento;
import co.com.pragma.model.credit.NotificacionEstado;
import co.com.pragma.model.gateway.CreditGateway;
import co.com.pragma.model.gateway.JsonConverter;
import co.com.pragma.model.gateway.NotificationEmailSQSGateway;
import co.com.pragma.usecase.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

@Log
@AllArgsConstructor
public class GuardarCapacidadEndeudamientoUseCase {

  private final CreditGateway creditGateway;
  private final NotificationEmailSQSGateway notificationEmailSQSGateway;
  private final JsonConverter jsonConverter;

  public Mono<Void> actualizarEstadoSolicitudCredito(MessageCapacidadEndeudamiento message) {
    Long idSolicitud = Long.valueOf(message.getIdSolicitud());

    return creditGateway.updateState(idSolicitud, message.getEstado())
            .flatMap(this::emitirNotificacion)
            .then();
  }

  private Mono<Void> emitirNotificacion(CreditParameters creditParameters) {
    String notificacionEstado = construirNotificacion(creditParameters);
    return notificationEmailSQSGateway.emit(notificacionEstado)
            .doOnSuccess(v -> log.info(Constants.LOG_SUCCESS))
            .doOnError(e -> log.severe(Constants.LOG_ERROR + e.getMessage()));
  }

  private String construirNotificacion(CreditParameters creditParameters) {
    NotificacionEstado notificacion = NotificacionEstado.builder()
            .estadoSolicitud(creditParameters.getEstado())
            .correoElectronico(creditParameters.getEmailNotification())
            .build();
    return jsonConverter.toJson(notificacion).orElse(notificacion.toString());
  }

}