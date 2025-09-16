package co.com.pragma.sqs.sender;

import co.com.pragma.sqs.sender.sendernotification.SQSSenderPropertiesNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.concurrent.CompletableFuture;

class SQSSenderNotificationTest {

  private SQSSenderPropertiesNotification properties;
  private SqsAsyncClient client;
  private SQSSenderNotification sender;

  @BeforeEach
  void setUp() {
    properties = Mockito.mock(SQSSenderPropertiesNotification.class);
    client = Mockito.mock(SqsAsyncClient.class);
    sender = new SQSSenderNotification(properties, client);
  }

  @Test
  void emit_shouldSendMessageSuccessfully() {
    String message = "test-message";
    Mockito.when(properties.queueUrl()).thenReturn("queue-url");
    SendMessageResponse response = SendMessageResponse.builder().messageId("123").build();
    Mockito.when(client.sendMessage(Mockito.any(SendMessageRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(response));

    Mono<Void> result = sender.emit(message);

    StepVerifier.create(result)
            .verifyComplete();
  }

  @Test
  void emit_shouldHandleError() {
    String message = "test-message";
    Mockito.when(properties.queueUrl()).thenReturn("queue-url");
    Mockito.when(client.sendMessage(Mockito.any(SendMessageRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("SQS error")));

    Mono<Void> result = sender.emit(message);

    StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
  }
}