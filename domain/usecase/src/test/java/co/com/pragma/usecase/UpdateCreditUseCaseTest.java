package co.com.pragma.usecase;

import co.com.pragma.model.credit.CreditApproved;
import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.model.credit.CreditReponse;
import co.com.pragma.model.gateway.CreditGateway;
import co.com.pragma.model.gateway.JsonConverter;
import co.com.pragma.model.gateway.JwtProvider;
import co.com.pragma.model.gateway.NotificacionSQSGateway;
import co.com.pragma.usecase.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.Mockito.*;

class UpdateCreditUseCaseTest {

  @Mock
  private CreditGateway creditGateway;

  @Mock
  private NotificacionSQSGateway notificacionSQSGateway;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private JsonConverter jsonConverter;

  @InjectMocks
  private UpdateCreditUseCase updateCreditUseCase;

  private CreditReponse creditResponse;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    creditResponse = CreditReponse.builder()
            .creditParameters(new CreditParameters())
            .statusResponse(Constants.STATUS_OK)
            .build();
  }

  @Test
  void shouldUpdateCreditStatusToApproved() {
    // Arrange
    Long id = 1L;
    CreditApproved request = new CreditApproved(true);
    creditResponse.getCreditParameters().setEstado("EN_REVISION");

    when(creditGateway.findById(id)).thenReturn(Mono.just(creditResponse));
    when(creditGateway.save(any())).thenReturn(Mono.just(creditResponse.getCreditParameters()));
    when(jsonConverter.toJson(any())).thenReturn(Optional.of("json-credit"));
    when(notificacionSQSGateway.emit(anyString())).thenReturn(Mono.empty());

    // Act
    Mono<CreditReponse> result = updateCreditUseCase.updateCreditStatus(id, request,"Bearer token");

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(r -> r.getCreditParameters().getEstado().equals(Constants.STATUS_APPROVED))
            .verifyComplete();

    verify(creditGateway).findById(id);
    verify(creditGateway).save(any());
    verify(notificacionSQSGateway).emit("json-credit");

  }

  @Test
  void shouldUpdateCreditStatusToRejected() {
    // Arrange
    Long id = 2L;
    CreditApproved request = new CreditApproved(false);

    when(creditGateway.findById(id)).thenReturn(Mono.just(creditResponse));
    when(creditGateway.save(any())).thenReturn(Mono.just(creditResponse.getCreditParameters()));
    when(jsonConverter.toJson(any())).thenReturn(Optional.of("json-credit"));
    when(notificacionSQSGateway.emit(anyString())).thenReturn(Mono.empty());

    // Act
    Mono<CreditReponse> result = updateCreditUseCase.updateCreditStatus(id, request,"Bearer token");

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(r -> r.getCreditParameters().getEstado().equals(Constants.STATUS_REJECTED))
            .verifyComplete();
  }

  @Test
  void shouldReturnNotFoundWhenCreditDoesNotExist() {
    // Arrange
    Long id = 3L;
    CreditApproved request = new CreditApproved(true);
    when(jwtProvider.getEmailFromToken(anyString())).thenReturn("test@example.com");
    when(creditGateway.findById(id)).thenReturn(Mono.empty());

    // Act
    Mono<CreditReponse> result = updateCreditUseCase.updateCreditStatus(id, request,"Bearer token");

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(r -> r.getStatusResponse().equals(Constants.STATUS_ERROR)
                    && r.getErrorMessage().equals(String.format(Constants.CREDIT_NOT_FOUND_MESSAGE, id)))
            .verifyComplete();
  }


  @Test
  void shouldHandleErrorWhenSavingFails() {
    // Arrange
    Long id = 4L;
    CreditApproved request = new CreditApproved(true);

    when(creditGateway.findById(id)).thenReturn(Mono.just(creditResponse));
    when(creditGateway.save(any())).thenReturn(Mono.error(new RuntimeException("DB error")));

    // Act
    Mono<CreditReponse> result = updateCreditUseCase.updateCreditStatus(id, request,"Bearer token");

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(r -> r.getStatusResponse().equals(Constants.STATUS_ERROR)
                    && r.getErrorMessage().equals("DB error"))
            .verifyComplete();
  }

  @Test
  void shouldEmitNotificationWithJson() {
    // Arrange
    Long id = 5L;
    CreditApproved request = new CreditApproved(true);

    when(creditGateway.findById(id)).thenReturn(Mono.just(creditResponse));
    when(creditGateway.save(any())).thenReturn(Mono.just(creditResponse.getCreditParameters()));
    when(jsonConverter.toJson(any())).thenReturn(Optional.of("json-credit"));
    when(notificacionSQSGateway.emit(anyString())).thenReturn(Mono.empty());

    // Act
    updateCreditUseCase.updateCreditStatus(id, request,"Bearer token").block();

    // Assert
    verify(notificacionSQSGateway).emit("json-credit");
  }

}
