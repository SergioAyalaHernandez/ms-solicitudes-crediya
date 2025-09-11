package co.com.pragma.usecase;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.model.credit.MessageCapacidadEndeudamiento;
import co.com.pragma.model.gateway.CreditGateway;
import co.com.pragma.model.gateway.JsonConverter;
import co.com.pragma.model.gateway.NotificationEmailSQSGateway;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GuardarCapacidadEndeudamientoUseCaseTest {

    private CreditGateway creditGateway;
    private NotificationEmailSQSGateway notificationEmailSQSGateway;
    private JsonConverter jsonConverter;
    private GuardarCapacidadEndeudamientoUseCase useCase;

    @BeforeEach
    void setUp() {
        creditGateway = mock(CreditGateway.class);
        notificationEmailSQSGateway = mock(NotificationEmailSQSGateway.class);
        jsonConverter = mock(JsonConverter.class);
        useCase = new GuardarCapacidadEndeudamientoUseCase(creditGateway, notificationEmailSQSGateway, jsonConverter);
    }

    @Test
    void actualizarEstadoSolicitudCredito_deberiaEmitirNotificacionYCompletar() {
        MessageCapacidadEndeudamiento message = new MessageCapacidadEndeudamiento();
        message.setIdSolicitud("123");
        message.setEstado("APROBADO");

        CreditParameters creditParameters = new CreditParameters();
        creditParameters.setEstado("APROBADO");
        creditParameters.setEmailNotification("test@email.com");

        when(creditGateway.updateState(any(Long.class), any(String.class))).thenReturn(Mono.just(creditParameters));
        when(jsonConverter.toJson(any())).thenReturn(Optional.of("{\"estado\":\"APROBADO\"}"));
        when(notificationEmailSQSGateway.emit(any(String.class))).thenReturn(Mono.empty());

        useCase.actualizarEstadoSolicitudCredito(message).block();

        verify(creditGateway).updateState(123L, "APROBADO");
        verify(notificationEmailSQSGateway).emit("{\"estado\":\"APROBADO\"}");
    }
}