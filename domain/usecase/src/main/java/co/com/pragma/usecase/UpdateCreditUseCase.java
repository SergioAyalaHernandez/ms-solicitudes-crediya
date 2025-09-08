package co.com.pragma.usecase;

import co.com.pragma.model.credit.CreditApproved;
import co.com.pragma.model.credit.CreditReponse;
import co.com.pragma.model.gateway.CreditGateway;
import co.com.pragma.model.gateway.JsonConverter;
import co.com.pragma.model.gateway.NotificacionSQSGateway;
import co.com.pragma.usecase.exceptions.NotFoundException;
import co.com.pragma.usecase.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Log
@RequiredArgsConstructor
public class UpdateCreditUseCase {

  private final CreditGateway creditGateway;
  private final NotificacionSQSGateway notificacionSQSGateway;
  private final JsonConverter jsonConverter;

  public Mono<CreditReponse> updateCreditStatus(Long id, CreditApproved requestBody) {
    String newStatus = requestBody.getApproved() ? Constants.STATUS_APPROVED : Constants.STATUS_REJECTED;

    return creditGateway.findById(id)
            .flatMap(credit -> updateCredit(credit, newStatus))
            .doOnNext(this::emitNotification)
            .switchIfEmpty(Mono.error(new NotFoundException(String.format(Constants.CREDIT_NOT_FOUND_MESSAGE, id))))
            .onErrorResume(this::handleError);
  }

  private Mono<CreditReponse> updateCredit(CreditReponse credit, String newStatus) {
    credit.getCreditParameters().setEstado(newStatus);
    return creditGateway.save(credit.getCreditParameters())
            .map(updatedCredit -> CreditReponse.builder()
                    .statusResponse(Constants.STATUS_OK)
                    .creditParameters(updatedCredit)
                    .build());
  }

  private void emitNotification(CreditReponse creditResponse) {
    String creditJson = String.valueOf(convertToJson(creditResponse));
    notificacionSQSGateway.emit(creditJson).subscribe();
  }

  private Optional<String> convertToJson(CreditReponse creditResponse) {
    return jsonConverter.toJson(creditResponse);
  }


  private Mono<CreditReponse> handleError(Throwable e) {
    log.severe(Constants.LOG_ERROR_UPDATE + e.getMessage());
    return Mono.just(CreditReponse.builder()
            .statusResponse(Constants.STATUS_ERROR)
            .errorMessage(e.getMessage())
            .build());
  }
}
