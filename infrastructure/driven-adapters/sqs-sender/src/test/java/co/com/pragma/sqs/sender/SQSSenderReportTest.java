package co.com.pragma.sqs.sender;

import co.com.pragma.sqs.sender.config.report.SQSSenderReportProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SQSSenderReportTest {
  @Mock
  private SQSSenderReportProperties properties;

  @Mock
  private SqsAsyncClient client;

  @InjectMocks
  private SQSSenderReport sqsSenderReport;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void emit_MessageSentSuccessfully() {
    // Arrange
    when(properties.queueUrl()).thenReturn("https://sqs.mock.url/report-queue");

    String message = "Test message";
    SendMessageResponse response = SendMessageResponse.builder().messageId("12345").build();


    when(client.sendMessage(any(SendMessageRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(response));

    // Act
    Mono<Void> result = sqsSenderReport.emit(message);

    // Assert
    StepVerifier.create(result)
            .verifyComplete();

    verify(client, times(1)).sendMessage(any(SendMessageRequest.class));
  }

  @Test
  void emit_ErrorSendingMessage() {
    // Arrange
    String message = "Test message";
    when(client.sendMessage(any(SendMessageRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("SQS error")));

    // Act
    Mono<Void> result = sqsSenderReport.emit(message);

    // Assert
    StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();

    verify(client, atLeastOnce()).sendMessage(any(SendMessageRequest.class));
  }


}