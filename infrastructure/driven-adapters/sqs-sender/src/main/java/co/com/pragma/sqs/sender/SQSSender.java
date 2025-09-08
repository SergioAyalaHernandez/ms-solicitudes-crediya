package co.com.pragma.sqs.sender;

import co.com.pragma.model.gateway.NotificacionSQSGateway;
import co.com.pragma.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements NotificacionSQSGateway {
    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;

    @Override
    public Mono<Void> emit(String message) {
        log.info("Intentando enviar mensaje a la cola SQS: {}", message);
        return Mono.fromCallable(() -> buildRequest(message))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.info("Mensaje enviado correctamente. MessageId: {}", response.messageId()))
                .doOnError(error -> log.error("Error al enviar mensaje a la cola SQS", error))
                .map(SendMessageResponse::messageId).then();
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }
}
