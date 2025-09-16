package co.com.pragma.usecase;

import co.com.pragma.model.credit.*;
import co.com.pragma.model.gateway.*;
import co.com.pragma.usecase.exceptions.ConstraintViolation;
import co.com.pragma.usecase.exceptions.ConstraintViolationException;
import co.com.pragma.usecase.gateway.GatewayExposeUser;
import co.com.pragma.usecase.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log
@RequiredArgsConstructor
public class CalculateDebtCapacityUseCase {
  private final CreditGateway creditGateway;
  private final GatewayExposeUser gatewayExposeUser;
  private final NotificacionSQSCapacidadGateway notificacionSQSCapacidadGateway;
  private final JsonConverter jsonConverter;
  private final JwtProvider jwtProvider;
  private final CreditTypeGateway creditTypeGateway;

  public Mono<CreditReponse> createCreditAutomatic(CreditParameters creditParameters, String token) {
    log.info(Constants.LOG_INIT_CREDIT_CREATION);
    String email = jwtProvider.getEmailFromToken(token);
    String saldo = String.valueOf(jwtProvider.getSalarioFromToken(token));
    return Mono.just(creditParameters)
            .flatMap(params -> validateJwtIdUser(params, token))
            .flatMap(this::validateCreditType)
            .flatMap(this::validateUserExistence)
            .doOnNext(creditReponse -> creditReponse.getCreditParameters().setEmailNotification(email))
            .flatMap(this::persistCredit)
            .flatMap(response -> emitNotificationInfo(response, creditParameters.getUserId(), saldo)
                    .flatMap(automaticCredit -> emitNotification(automaticCredit, response)))
            .onErrorResume(this::handleError)
            .doOnSuccess(response -> log.info(Constants.LOG_END_CREDIT_CREATION));
  }


  private Mono<CreditReponse> emitNotification(AutomaticCredit creditResponse, CreditReponse creditReponse) {
    convertToJson(creditResponse).ifPresent(creditJson ->
            notificacionSQSCapacidadGateway.emit(creditJson).subscribe()
    );
    return Mono.just(creditReponse);
  }


  private Mono<AutomaticCredit> emitNotificationInfo(CreditReponse creditResponse, Long userId, String saldo) {

    return obtenerPrestamosActivos(userId)
            .flatMap(prestamosActivos -> {
              AutomaticCredit automaticCredit = AutomaticCredit.builder()
                      .solicitudId(String.valueOf(creditResponse.getCreditParameters().getIdEntidadGuardada()))
                      .tipoPrestamoId(String.valueOf(creditResponse.getCreditParameters().getIdEntidadGuardada()))
                      .montoSolicitado(creditResponse.getCreditParameters().getMonto())
                      .plazoMeses(creditResponse.getCreditParameters().getPlazoMeses())
                      .tasaInteresMensual(creditResponse.getCreditParameters().getTasaInteres())
                      .ingresosTotales(new BigDecimal(saldo))
                      .prestamosActivos(prestamosActivos)
                      .correoSolicitante(creditResponse.getCreditParameters().getEmailNotification())
                      .fechaSolicitud(LocalDateTime.now())
                      .build();
              return Mono.just(automaticCredit);
            });
  }

  Mono<List<PrestamoActivo>> obtenerPrestamosActivos(Long userId) {
    return creditGateway.findAllCredits(String.valueOf(userId))
            .filter(credit -> Constants.STATUS_APPROVED.equals(credit.getEstado()))
            .map(credit -> PrestamoActivo.builder()
                    .prestamoId(String.valueOf(credit.getIdEntidadGuardada()))
                    .monto(credit.getMonto().doubleValue())
                    .plazoMeses(credit.getPlazoMeses())
                    .tasaInteresMensual(credit.getTasaInteres().doubleValue())
                    .build())
            .collectList();
  }

  private Mono<CreditReponse> validateCreditType(CreditReponse response) {
    log.info(Constants.LOG_VALIDATING_CREDIT_TYPE);
    if (Constants.STATUS_ERROR.equals(response.getStatusResponse())) {
      log.warning(Constants.LOG_SKIP_CREDIT_TYPE_VALIDATION);
      return Mono.just(response);
    }

    CreditParameters params = response.getCreditParameters();
    log.info(Constants.LOG_QUERYING_CREDIT_TYPE + params.getTipoPrestamo());

    return creditTypeGateway.getCreditTypeById(params.getTipoPrestamo())
            .map(idTipoCredito -> {
              params.setEstado(Constants.ESTADO_PENDIENTE_REVISION);
              return buildResponse(Constants.STATUS_VALID_TYPE, params, null);
            });
  }

  Mono<CreditReponse> validateJwtIdUser(CreditParameters parameters, String token) {
    String userId = jwtProvider.getUserIdFromToken(token);
    if (!String.valueOf(parameters.getUserId()).equals(userId)) {
      log.warning(Constants.LOG_USER_ID_MISMATCH);
      return buildErrorResponse(parameters)
              .map(response -> {
                response.setErrorMessage(Constants.MSG_UNAUTHORIZED_USER);
                return response;
              });
    }
    return buildResponseMono(Constants.STATUS_VALID_USER, parameters, null);
  }

  private Mono<CreditReponse> persistCredit(CreditReponse response) {
    log.info(Constants.LOG_INIT_CREDIT_PERSISTENCE);
    if (Constants.STATUS_ERROR.equals(response.getStatusResponse())) {
      log.warning(Constants.LOG_SKIP_CREDIT_PERSISTENCE);
      return Mono.just(response);
    }

    CreditParameters params = response.getCreditParameters();
    log.info(Constants.LOG_SAVING_CREDIT + params.getUserId());

    return creditGateway.createCredit(params)
            .doOnSuccess(__ -> log.info(Constants.LOG_CREDIT_SAVED));
  }

  private Mono<CreditReponse> validateUserExistence(CreditReponse response) {
    CreditParameters params = response.getCreditParameters();
    log.info(Constants.LOG_VALIDATING_USER + params.getDocumentNumber());
    return gatewayExposeUser.findByDocument(params.getDocumentNumber())
            .flatMap(userId -> {
              if (userId == null || userId == 0L) {
                return buildErrorResponse(params);
              }
              params.setUserId(userId);
              return buildResponseMono(Constants.STATUS_VALID_USER, params, null);
            });
  }

  private Optional<String> convertToJson(AutomaticCredit creditResponse) {
    return jsonConverter.toJson(creditResponse);
  }

  private CreditReponse buildResponse(String status, CreditParameters params, String errorMessage) {
    return CreditReponse.builder()
            .statusResponse(status)
            .creditParameters(params)
            .errorMessage(errorMessage)
            .build();
  }

  String extractErrorMessage(Throwable e) {
    log.info(Constants.LOG_EXTRACTING_ERROR_MESSAGE + e.getClass().getSimpleName());
    if (e instanceof ConstraintViolationException) {
      return ((ConstraintViolationException) e).getConstraintViolations().stream()
              .map(ConstraintViolation::getMessage)
              .collect(Collectors.joining(Constants.COMMA_SEPARATOR));
    }
    return Constants.MSG_UNEXPECTED_ERROR;
  }

  Mono<CreditReponse> handleError(Throwable e) {
    log.severe(Constants.LOG_ERROR_CREDIT_CREATION + e.getMessage());
    String errorMessage = extractErrorMessage(e);
    log.info(Constants.LOG_ERROR_MESSAGE_EXTRACTED + errorMessage);
    return buildResponseMono(Constants.STATUS_ERROR, new CreditParameters(), errorMessage);
  }

  private Mono<CreditReponse> buildResponseMono(String status, CreditParameters params, String errorMessage) {
    return Mono.just(buildResponse(status, params, errorMessage));
  }

  private Mono<CreditReponse> buildErrorResponse(CreditParameters params) {
    return buildResponseMono(Constants.STATUS_ERROR, params, Constants.MSG_USER_NOT_FOUND);
  }

}
