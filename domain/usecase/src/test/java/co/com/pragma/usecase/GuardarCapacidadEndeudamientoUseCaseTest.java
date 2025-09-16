package co.com.pragma.usecase;

import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.model.credit.MessageCapacidadEndeudamiento;
import co.com.pragma.model.gateway.CreditGateway;
import co.com.pragma.model.gateway.JsonConverter;
import co.com.pragma.model.gateway.NotificationEmailSQSGateway;
import co.com.pragma.model.gateway.ReportSQSGateway;
import co.com.pragma.usecase.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GuardarCapacidadEndeudamientoUseCaseTest {

  private CreditGateway creditGateway;
  private NotificationEmailSQSGateway notificationEmailSQSGateway;
  private JsonConverter jsonConverter;
  private ReportSQSGateway reportSQSGateway;

  private GuardarCapacidadEndeudamientoUseCase useCase;

  @BeforeEach
  void setUp() {
    creditGateway = mock(CreditGateway.class);
    notificationEmailSQSGateway = mock(NotificationEmailSQSGateway.class);
    jsonConverter = mock(JsonConverter.class);
    reportSQSGateway = mock(ReportSQSGateway.class);

    useCase = new GuardarCapacidadEndeudamientoUseCase(
            creditGateway, notificationEmailSQSGateway, jsonConverter, reportSQSGateway);
  }

  @Test
  void actualizarEstadoCredito_aprobado_debeEmitirReporteYNotificacion() {
    // Arrange
    MessageCapacidadEndeudamiento message = new MessageCapacidadEndeudamiento();
    message.setIdSolicitud("123");
    message.setEstado(Constants.STATUS_APPROVED_LAMDA);

    CreditParameters credit = CreditParameters.builder()
            .monto(BigDecimal.valueOf(5000.0))
            .estado(Constants.STATUS_APPROVED_LAMDA)
            .emailNotification("test@mail.com")
            .build();

    when(creditGateway.updateState(123L, Constants.STATUS_APPROVED_LAMDA))
            .thenReturn(Mono.just(credit));
    when(jsonConverter.toJson(any())).thenReturn(Optional.of("{json}"));
    when(reportSQSGateway.emit("{json}")).thenReturn(Mono.empty());
    when(notificationEmailSQSGateway.emit("{json}")).thenReturn(Mono.empty());

    // Act & Assert
    StepVerifier.create(useCase.actualizarEstadoSolicitudCredito(message))
            .verifyComplete();

    verify(reportSQSGateway, times(1)).emit("{json}");
    verify(notificationEmailSQSGateway, times(1)).emit("{json}");
  }

  @Test
  void actualizarEstadoCredito_noAprobado_soloNotificacion() {
    // Arrange
    MessageCapacidadEndeudamiento message = new MessageCapacidadEndeudamiento();
    message.setIdSolicitud("456");
    message.setEstado("RECHAZADO");

    CreditParameters credit = CreditParameters.builder()
            .monto(BigDecimal.valueOf(0.0))
            .estado("RECHAZADO")
            .emailNotification("rechazado@mail.com")
            .build();

    when(creditGateway.updateState(456L, "RECHAZADO"))
            .thenReturn(Mono.just(credit));
    when(jsonConverter.toJson(any())).thenReturn(Optional.of("{json}"));
    when(notificationEmailSQSGateway.emit("{json}")).thenReturn(Mono.empty());

    // Act & Assert
    StepVerifier.create(useCase.actualizarEstadoSolicitudCredito(message))
            .verifyComplete();

    verify(reportSQSGateway, never()).emit(any());
    verify(notificationEmailSQSGateway, times(1)).emit("{json}");
  }

  @Test
  void actualizarEstadoCredito_errorEnNotificacion_noRompeFlujo() {
    // Arrange
    MessageCapacidadEndeudamiento message = new MessageCapacidadEndeudamiento();
    message.setIdSolicitud("789");
    message.setEstado(Constants.STATUS_APPROVED_LAMDA);

    CreditParameters credit = CreditParameters.builder()
            .monto(BigDecimal.valueOf(1000.0))
            .estado(Constants.STATUS_APPROVED_LAMDA)
            .emailNotification("fail@mail.com")
            .build();

    when(creditGateway.updateState(789L, Constants.STATUS_APPROVED_LAMDA))
            .thenReturn(Mono.just(credit));
    when(jsonConverter.toJson(any())).thenReturn(Optional.of("{json}"));
    when(reportSQSGateway.emit("{json}")).thenReturn(Mono.empty());
    when(notificationEmailSQSGateway.emit("{json}"))
            .thenReturn(Mono.error(new RuntimeException("Error al enviar notificación")));

    // Act & Assert
    StepVerifier.create(useCase.actualizarEstadoSolicitudCredito(message))
            .verifyComplete(); // el flujo debe terminar normalmente

    verify(reportSQSGateway, times(1)).emit("{json}");
    verify(notificationEmailSQSGateway, times(1)).emit("{json}");
  }
}