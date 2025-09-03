package co.com.pragma.consumer;

import co.com.pragma.consumer.utils.Constants;
import co.com.pragma.usecase.gateway.GatewayExposeUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Log
public class RestConsumer implements GatewayExposeUser {
  private final WebClient client;

  @Override
  public Mono<Long> findByDocument(Long document) {
    log.info(Constants.LOG_INIT_QUERY + document);

    return client.get()
            .uri(Constants.USER_BY_DOCUMENT_PATH, document)
            .retrieve()
            .bodyToMono(ObjectResponse.class)
            .doOnSuccess(response -> log.info(Constants.LOG_SUCCESS_RESPONSE
                    + document + ": " + (response != null)))
            .flatMap(this::mapResponseToId)
            .onErrorResume(error -> handleError(error, document))
            .doOnNext(id -> log.info(Constants.LOG_RETURNED_ID + document + ": " + id));
  }

  private Mono<Long> mapResponseToId(ObjectResponse response) {
    return response != null ? Mono.just(response.getId()) : Mono.empty();
  }

  private Mono<Long> handleError(Throwable error, Long document) {
    String message = error.getMessage();
    if (message != null && message.contains(Constants.ERROR_404)) {
      log.info(Constants.LOG_USER_NOT_FOUND + document + ": " + message);
      return Mono.just(0L);
    }
    log.warning(Constants.LOG_GENERIC_ERROR + document + ": " + message);
    return Mono.empty();
  }
}
