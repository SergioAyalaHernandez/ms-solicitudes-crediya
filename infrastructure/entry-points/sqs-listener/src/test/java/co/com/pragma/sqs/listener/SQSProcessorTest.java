package co.com.pragma.sqs.listener;

import co.com.pragma.model.credit.MessageCapacidadEndeudamiento;
import co.com.pragma.usecase.GuardarCapacidadEndeudamientoUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class SQSProcessorTest {
  @Test
  void apply_shouldProcessMessageAndCallUseCase() throws Exception {
    // Arrange
    GuardarCapacidadEndeudamientoUseCase useCase = mock(GuardarCapacidadEndeudamientoUseCase.class);
    Message message = mock(Message.class);
    String body = "{\"idSolicitud\":\"123\",\"estado\":\"APROBADO\"}";
    when(message.body()).thenReturn(body);
    when(useCase.actualizarEstadoSolicitudCredito(any()))
            .thenReturn(Mono.empty());
    SQSProcessor processor = new SQSProcessor(useCase);

    // Act
    processor.apply(message).block();

    // Assert
    org.mockito.Mockito.verify(useCase).actualizarEstadoSolicitudCredito(org.mockito.Mockito.any());
  }

  @Test
  void apply_shouldThrowRuntimeExceptionOnError() {
    GuardarCapacidadEndeudamientoUseCase useCase = mock(GuardarCapacidadEndeudamientoUseCase.class);
    Message message = mock(Message.class);
    when(message.body()).thenReturn("invalid-json");
    SQSProcessor processor = new SQSProcessor(useCase);

    Assertions.assertThrows(RuntimeException.class, () -> {
      processor.apply(message).block();
    });
  }

  @Test
  void convertirMensaje_shouldReturnParsedObject() throws Exception {
    SQSProcessor processor = new SQSProcessor(mock(GuardarCapacidadEndeudamientoUseCase.class));
    String body = "{\"idSolicitud\":\"123\",\"estado\":\"APROBADO\"}";
    MessageCapacidadEndeudamiento result = processor.convertirMensaje(body);
    assertNotNull(result);
    assertEquals("123", result.getIdSolicitud());
    assertEquals("APROBADO", result.getEstado());
  }
}