package co.com.pragma.api.credit;

import co.com.pragma.api.utils.Constants;
import co.com.pragma.model.credit.CreditApproved;
import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.usecase.CalculateDebtCapacityUseCase;
import co.com.pragma.usecase.CreateCreditUseCase;
import co.com.pragma.usecase.CreditListUseCase;
import co.com.pragma.usecase.UpdateCreditUseCase;
import co.com.pragma.usecase.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CreditHandler {

  private final CreateCreditUseCase createCreditUseCase;
  private final CreditListUseCase creditListUseCase;
  private final UpdateCreditUseCase updateCreditUseCase;
  private final CalculateDebtCapacityUseCase calculateDebtCapacityUseCase;

  public Mono<ServerResponse> createCredit(ServerRequest request) {
    String token = request.headers().firstHeader(Constants.HEADER_AUTHORIZATION);

    return request.bodyToMono(CreditParameters.class)
            .flatMap(creditParams -> createCreditUseCase.createCredit(creditParams, token))
            .flatMap(credit -> ServerResponse.ok().bodyValue(credit))
            .onErrorResume(error -> ServerResponse.status(500)
                    .bodyValue(Constants.ERROR_CREATE_CREDIT + error.getMessage()));
  }

  public Mono<ServerResponse> getCreditsList(ServerRequest request) {
    String token = request.headers().firstHeader(Constants.HEADER_AUTHORIZATION);
    int page = Integer.parseInt(request.queryParam(Constants.PAGE).orElse(Constants.DEFAULT_PAGE));
    int size = Integer.parseInt(request.queryParam(Constants.SIZE).orElse(Constants.DEFAULT_SIZE));

    return creditListUseCase.getCreditsList(page, size, token)
            .collectList()
            .flatMap(list -> ServerResponse.ok().bodyValue(list))
            .onErrorResume(error -> ServerResponse.status(500)
                    .bodyValue(Constants.ERROR_GET_CREDITS + error.getMessage()));
  }

  public Mono<ServerResponse> updateCredit(ServerRequest request) {
    Long id = Long.valueOf(request.pathVariable(Constants.ID));
    String token = request.headers().firstHeader(Constants.HEADER_AUTHORIZATION);
    return request.bodyToMono(CreditApproved.class)
            .flatMap(creditParams -> updateCreditUseCase.updateCreditStatus(id, creditParams, token))
            .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
            .onErrorResume(error -> {
              if (error instanceof NotFoundException) {
                return ServerResponse.status(404)
                        .bodyValue(Constants.ERROR_CREDIT_NOT_FOUND + error.getMessage());
              }
              return ServerResponse.status(500)
                      .bodyValue(Constants.ERROR_UPDATE_CREDIT + error.getMessage());
            });
  }

  public Mono<ServerResponse> calculateDebtCapacity(ServerRequest request) {
    String token = request.headers().firstHeader(Constants.HEADER_AUTHORIZATION);
    return request.bodyToMono(CreditParameters.class)
            .flatMap(credit -> calculateDebtCapacityUseCase.createCreditAutomatic(credit, token))
            .flatMap(credit -> ServerResponse.ok().bodyValue(credit))
            .onErrorResume(error -> ServerResponse.status(500)
                    .bodyValue(Constants.ERROR_CREATE_CREDIT + error.getMessage()));
  }
}
