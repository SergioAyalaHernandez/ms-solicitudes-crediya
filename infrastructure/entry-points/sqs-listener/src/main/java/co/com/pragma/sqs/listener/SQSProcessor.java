package co.com.pragma.sqs.listener;

import co.com.pragma.model.credit.MessageCapacidadEndeudamiento;
import co.com.pragma.sqs.listener.utils.Constants;
import co.com.pragma.usecase.GuardarCapacidadEndeudamientoUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
  private final GuardarCapacidadEndeudamientoUseCase myUseCase;

  @Override
  public Mono<Void> apply(Message message) {
    try {
      String body = message.body();
      log.info(Constants.LOG_BODY_MESSAGE + "{}", body);
      myUseCase.actualizarEstadoSolicitudCredito(convertirMensaje(body)).subscribe();
    } catch (Exception ex) {
      log.error(Constants.ERROR_PROCESSING_MESSAGE, ex);
      throw new RuntimeException(Constants.ERROR_PROCESSING_MESSAGE, ex);
    }
    return Mono.empty();
  }


  private MessageCapacidadEndeudamiento convertirMensaje(String body) throws com.fasterxml.jackson.core.JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(body, MessageCapacidadEndeudamiento.class);
  }
}
