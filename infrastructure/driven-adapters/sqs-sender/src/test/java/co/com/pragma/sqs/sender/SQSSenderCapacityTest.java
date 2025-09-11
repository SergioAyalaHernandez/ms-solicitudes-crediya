package co.com.pragma.sqs.sender;

import co.com.pragma.sqs.sender.config.capacity.SQSSenderPropertiesCapacity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.concurrent.CompletableFuture;

class SQSSenderCapacityTest {

  private SQSSenderPropertiesCapacity properties;
  private SqsAsyncClient client;
  private SQSSenderCapacity sender;

  @BeforeEach
  void setUp() {
    properties = Mockito.mock(SQSSenderPropertiesCapacity.class);
    client = Mockito.mock(SqsAsyncClient.class);
    sender = new SQSSenderCapacity(properties, client);

    Mockito.when(properties.queueUrl()).thenReturn("test-queue-url");
  }

  @Test
  void emit_shouldSendMessageSuccessfully() {
    String message = "test-message";
    SendMessageResponse response = SendMessageResponse.builder().messageId("123").build();
    CompletableFuture<SendMessageResponse> future = CompletableFuture.completedFuture(response);

    Mockito.when(client.sendMessage(Mockito.any(SendMessageRequest.class))).thenReturn(future);

    Mono<Void> result = sender.emit(message);

    StepVerifier.create(result)
            .verifyComplete();

    Mockito.verify(client).sendMessage(Mockito.any(SendMessageRequest.class));
  }

  @Test
  void emit_shouldHandleError() {
    String message = "test-message";
    CompletableFuture<SendMessageResponse> future = new CompletableFuture<>();
    future.completeExceptionally(new RuntimeException("SQS error"));

    Mockito.when(client.sendMessage(Mockito.any(SendMessageRequest.class))).thenReturn(future);

    Mono<Void> result = sender.emit(message);

    StepVerifier.create(result)
            .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                    && throwable.getMessage().equals("SQS error"))
            .verify();

    Mockito.verify(client).sendMessage(Mockito.any(SendMessageRequest.class));
  }
}