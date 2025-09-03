package co.com.pragma.usecase;

import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.model.credit.CreditReponse;
import co.com.pragma.model.gateway.CreditGateway;
import co.com.pragma.model.gateway.CreditTypeGateway;
import co.com.pragma.model.gateway.JwtProvider;
import co.com.pragma.usecase.exceptions.ConstraintViolation;
import co.com.pragma.usecase.exceptions.ConstraintViolationException;
import co.com.pragma.usecase.gateway.GatewayExposeUser;
import co.com.pragma.usecase.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCreditUseCaseTest {

  @Mock
  private GatewayExposeUser gatewayExposeUser;

  @Mock
  private CreditTypeGateway creditTypeGateway;

  @Mock
  private CreditGateway creditGateway;

  @Mock
  private JwtProvider jwtProvider;

  private CreateCreditUseCase createCreditUseCase;
  private static final String TOKEN = "valid-token";
  private static final Long USER_ID = 123L;
  private static final String DOCUMENT_NUMBER = "1234567890";
  private static final Long CREDIT_TYPE_ID = 1L;

  @BeforeEach
  void setUp() {
    createCreditUseCase = new CreateCreditUseCase(
            gatewayExposeUser,
            creditTypeGateway,
            creditGateway,
            jwtProvider
    );
  }

  @Test
  void createCredit_Success_ReturnsValidCreditResponse() {
    // Arrange
    CreditParameters creditParameters = new CreditParameters();
    creditParameters.setUserId(USER_ID);
    creditParameters.setDocumentNumber(Long.valueOf(DOCUMENT_NUMBER));
    creditParameters.setTipoPrestamo(CREDIT_TYPE_ID);

    when(jwtProvider.getUserIdFromToken(TOKEN)).thenReturn(String.valueOf(USER_ID));
    when(creditTypeGateway.getCreditTypeById(CREDIT_TYPE_ID)).thenReturn(Mono.just(CREDIT_TYPE_ID));
    when(gatewayExposeUser.findByDocument(Long.valueOf(DOCUMENT_NUMBER))).thenReturn(Mono.just(USER_ID));
    when(creditGateway.createCredit(any(CreditParameters.class))).thenReturn(Mono.empty());

    // Act
    Mono<CreditReponse> result = createCreditUseCase.createCredit(creditParameters, TOKEN);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response ->
                    Constants.STATUS_OK.equals(response.getStatusResponse()) &&
                            response.getErrorMessage() == null &&
                            response.getCreditParameters().equals(creditParameters))
            .verifyComplete();
  }

  @Test
  void createCredit_InvalidJwt_ReturnsErrorResponse() {
    // Arrange
    CreditParameters creditParameters = new CreditParameters();
    creditParameters.setUserId(USER_ID);
    creditParameters.setDocumentNumber(Long.valueOf(DOCUMENT_NUMBER));
    creditParameters.setTipoPrestamo(CREDIT_TYPE_ID);

    when(jwtProvider.getUserIdFromToken(TOKEN)).thenReturn("456");

    when(gatewayExposeUser.findByDocument(Long.valueOf(DOCUMENT_NUMBER)))
            .thenReturn(Mono.just(USER_ID));

    when(creditGateway.createCredit(any(CreditParameters.class)))
            .thenReturn(Mono.error(new RuntimeException(Constants.MSG_UNAUTHORIZED_USER)));


    // Act
    Mono<CreditReponse> result = createCreditUseCase.createCredit(creditParameters, TOKEN);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response ->
                    Constants.STATUS_ERROR.equals(response.getStatusResponse()) &&
                            "Error inesperado".equals(response.getErrorMessage()))
            .verifyComplete();

  }

  @Test
  void createCredit_UserNotFound_ReturnsErrorResponse() {
    // Arrange
    CreditParameters creditParameters = new CreditParameters();
    creditParameters.setUserId(USER_ID);
    creditParameters.setDocumentNumber(Long.valueOf(DOCUMENT_NUMBER));

    when(jwtProvider.getUserIdFromToken(TOKEN)).thenReturn(String.valueOf(USER_ID));
    when(creditTypeGateway.getCreditTypeById(any())).thenReturn(Mono.just(CREDIT_TYPE_ID));
    when(gatewayExposeUser.findByDocument(Long.valueOf(DOCUMENT_NUMBER))).thenReturn(Mono.just(0L)); // Usuario no encontrado

    // Act
    Mono<CreditReponse> result = createCreditUseCase.createCredit(creditParameters, TOKEN);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response ->
                    Constants.STATUS_ERROR.equals(response.getStatusResponse()) &&
                            Constants.MSG_USER_NOT_FOUND.equals(response.getErrorMessage()))
            .verifyComplete();
  }

  @Test
  void createCredit_ConstraintViolation_ReturnsErrorResponse() {
    // Arrange
    CreditParameters creditParameters = new CreditParameters();
    creditParameters.setUserId(USER_ID);
    creditParameters.setDocumentNumber(Long.valueOf(DOCUMENT_NUMBER));
    creditParameters.setTipoPrestamo(CREDIT_TYPE_ID);

    String errorMessage = "Validation error";
    ConstraintViolation violation = new ConstraintViolation(errorMessage);

    when(jwtProvider.getUserIdFromToken(TOKEN)).thenReturn(String.valueOf(USER_ID));
    when(creditTypeGateway.getCreditTypeById(CREDIT_TYPE_ID)).thenReturn(Mono.just(CREDIT_TYPE_ID));
    when(gatewayExposeUser.findByDocument(Long.valueOf(DOCUMENT_NUMBER))).thenReturn(Mono.just(USER_ID));
    when(creditGateway.createCredit(any(CreditParameters.class)))
            .thenReturn(Mono.error(new ConstraintViolationException(Collections.singletonList(violation))));

    // Act
    Mono<CreditReponse> result = createCreditUseCase.createCredit(creditParameters, TOKEN);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response ->
                    Constants.STATUS_ERROR.equals(response.getStatusResponse()) &&
                            errorMessage.equals(response.getErrorMessage()))
            .verifyComplete();
  }

  @Test
  void createCredit_UnexpectedError_ReturnsGenericErrorResponse() {
    // Arrange
    CreditParameters creditParameters = new CreditParameters();
    creditParameters.setUserId(USER_ID);
    creditParameters.setDocumentNumber(Long.valueOf(DOCUMENT_NUMBER));
    creditParameters.setTipoPrestamo(CREDIT_TYPE_ID);

    when(jwtProvider.getUserIdFromToken(TOKEN)).thenReturn(String.valueOf(USER_ID));
    when(creditTypeGateway.getCreditTypeById(CREDIT_TYPE_ID)).thenReturn(Mono.just(CREDIT_TYPE_ID));
    when(gatewayExposeUser.findByDocument(Long.valueOf(DOCUMENT_NUMBER))).thenReturn(Mono.just(USER_ID));
    when(creditGateway.createCredit(any(CreditParameters.class)))
            .thenReturn(Mono.error(new RuntimeException("Unexpected error")));

    // Act
    Mono<CreditReponse> result = createCreditUseCase.createCredit(creditParameters, TOKEN);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response ->
                    Constants.STATUS_ERROR.equals(response.getStatusResponse()) &&
                            Constants.MSG_UNEXPECTED_ERROR.equals(response.getErrorMessage()))
            .verifyComplete();
  }
}
