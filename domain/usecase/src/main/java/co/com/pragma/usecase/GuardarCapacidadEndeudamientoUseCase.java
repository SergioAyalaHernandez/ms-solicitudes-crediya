package co.com.pragma.usecase;

import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.model.credit.MessageCapacidadEndeudamiento;
import co.com.pragma.model.credit.MontoAprobado;
import co.com.pragma.model.credit.NotificacionEstado;
import co.com.pragma.model.gateway.CreditGateway;
import co.com.pragma.model.gateway.JsonConverter;
import co.com.pragma.model.gateway.NotificationEmailSQSGateway;
import co.com.pragma.model.gateway.ReportSQSGateway;
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
  private final ReportSQSGateway reportSQSGateway;

  public Mono<Void> actualizarEstadoSolicitudCredito(MessageCapacidadEndeudamiento message) {
    Long idSolicitud = Long.valueOf(message.getIdSolicitud());
    return creditGateway.updateState(idSolicitud, message.getEstado())
            .flatMap(credit -> processApprovedCredit(credit, message.getEstado()))
            .flatMap(this::emitirNotificacion)
            .then();
  }

  private Mono<CreditParameters> processApprovedCredit(CreditParameters credit, String newStatus) {
    if (newStatus.equals(Constants.STATUS_APPROVED_LAMDA)) {
      MontoAprobado montoAprobado = MontoAprobado.builder()
              .montoAprobado(String.valueOf(credit.getMonto()))
              .build();
      jsonConverter.toJson(montoAprobado).ifPresent(json -> reportSQSGateway.emit(json).subscribe());
    }
    return Mono.just(credit);
  }

  private Mono<Void> emitirNotificacion(CreditParameters creditParameters) {
    String notificacionEstado = construirNotificacion(creditParameters);
    return notificationEmailSQSGateway.emit(notificacionEstado)
            .doOnSuccess(v -> log.info(Constants.LOG_SUCCESS))
            .doOnError(e -> log.severe(Constants.LOG_ERROR + e.getMessage()))
            .onErrorResume(e -> Mono.empty());
  }

  private String construirNotificacion(CreditParameters creditParameters) {
    NotificacionEstado notificacion = NotificacionEstado.builder()
            .estadoSolicitud(creditParameters.getEstado())
            .correoElectronico(creditParameters.getEmailNotification())
            .build();
    return jsonConverter.toJson(notificacion).orElse(notificacion.toString());
  }

}