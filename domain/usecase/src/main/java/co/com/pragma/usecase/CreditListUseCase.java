package co.com.pragma.usecase;

import co.com.pragma.model.credit.CreditDetailDTO;
import co.com.pragma.model.credit.CreditListResponse;
import co.com.pragma.model.gateway.CreditGateway;
import co.com.pragma.usecase.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;

@Log
@RequiredArgsConstructor
public class CreditListUseCase {
  private final CreditGateway creditGateway;

  public Flux<CreditListResponse> getCreditsList(int page, int size, String token) {
    log.info(Constants.LOG_FETCHING_CREDITS_LIST + page + Constants.SIZE+ size);

    Mono<Long> totalCreditsMono = creditGateway.findSizeAllCredits();
    Flux<CreditDetailDTO> creditsFlux = creditGateway.findAllCredits(page, size, token);

      return buildCreditListResponse(creditsFlux, totalCreditsMono);
  }

  private Flux<CreditListResponse> buildCreditListResponse(Flux<CreditDetailDTO> creditsFlux, Mono<Long> totalCreditsMono) {
    return creditsFlux
            .collectList()
            .zipWith(totalCreditsMono)
              .map(this::mapToCreditListResponse)
              .flux()
              .onErrorResume(this::handleError);
  }

  private CreditListResponse mapToCreditListResponse(Tuple2<List<CreditDetailDTO>, Long> tuple) {
              List<CreditDetailDTO> creditDetails = tuple.getT1();
              Long totalCredits = tuple.getT2();
              return CreditListResponse.builder()
                      .totalCredits(totalCredits)
                      .creditDetailDTO(creditDetails)
                      .statusResponse(Constants.STATUS_OK)
                      .errorMessage(null)
                      .build();
  }

  private Flux<CreditListResponse> handleError(Throwable error) {
              log.severe(Constants.LOG_ERROR_FETCHING_CREDITS + ": " + error.getMessage());
              return Flux.just(
                      CreditListResponse.builder()
                              .totalCredits(0L)
                              .statusResponse(Constants.STATUS_ERROR)
                              .creditDetailDTO(List.of())
                              .errorMessage(error.getMessage())
                              .build()
              );
  }
}


