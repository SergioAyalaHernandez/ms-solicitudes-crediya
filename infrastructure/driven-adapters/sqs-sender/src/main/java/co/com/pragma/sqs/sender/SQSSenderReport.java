package co.com.pragma.sqs.sender;

import co.com.pragma.model.gateway.ReportSQSGateway;
import co.com.pragma.sqs.sender.config.report.SQSSenderReportProperties;
import co.com.pragma.sqs.sender.utils.Constants;
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
public class SQSSenderReport implements ReportSQSGateway {
  private final SQSSenderReportProperties properties;
  private final SqsAsyncClient sqsAsyncClient;

  @Override
  public Mono<Void> emit(String message) {
    log.info(Constants.LOG_SENDING_MESSAGE_REPORT, message);
    return Mono.fromCallable(() -> buildRequest(message))
            .flatMap(request -> Mono.fromFuture(sqsAsyncClient.sendMessage(request)))
            .doOnNext(response -> log.info(Constants.LOG_MESSAGE_SENT_REPORT, response.messageId()))
            .doOnError(error -> log.error(Constants.LOG_ERROR_SENDING_MESSAGE, error))
            .map(SendMessageResponse::messageId).then();
  }

  private SendMessageRequest buildRequest(String message) {
    return SendMessageRequest.builder()
            .queueUrl(properties.queueUrl())
            .messageBody(message)
            .build();
  }
}
