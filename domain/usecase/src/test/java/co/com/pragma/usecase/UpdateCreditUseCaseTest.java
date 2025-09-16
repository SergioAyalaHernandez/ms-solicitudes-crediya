package co.com.pragma.usecase;

import co.com.pragma.model.credit.CreditApproved;
import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.model.credit.CreditReponse;
import co.com.pragma.model.gateway.*;
import co.com.pragma.usecase.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.*;

class UpdateCreditUseCaseTest {

  private CreditGateway creditGateway;
  private NotificacionSQSGateway notificacionSQSGateway;
  private ReportSQSGateway reportSQSGateway;
  private JsonConverter jsonConverter;
  private JwtProvider jwtProvider;
  private UpdateCreditUseCase useCase;

  @BeforeEach
  void setUp() {
    creditGateway = mock(CreditGateway.class);
    notificacionSQSGateway = mock(NotificacionSQSGateway.class);
    reportSQSGateway = mock(ReportSQSGateway.class);
    jsonConverter = mock(JsonConverter.class);
    jwtProvider = mock(JwtProvider.class);

    useCase = new UpdateCreditUseCase(
            creditGateway, notificacionSQSGateway, reportSQSGateway, jsonConverter, jwtProvider);
  }

  @Test
  void updateCreditStatus_creditFoundAndApproved() {
    // Arrange
    Long id = 1L;
    String token = "jwt.token";
    CreditApproved request = new CreditApproved(true);

    CreditParameters params = CreditParameters.builder()
            .monto(BigDecimal.valueOf(10000.0))
            .estado("PENDING")
            .build();

    CreditReponse existing = CreditReponse.builder()
            .creditParameters(params)
            .build();

    when(jwtProvider.getEmailFromToken(token)).thenReturn("user@mail.com");
    when(creditGateway.findById(id)).thenReturn(Mono.just(existing));
    when(creditGateway.save(any(CreditParameters.class)))
            .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
    when(jsonConverter.toJson(any())).thenReturn(Optional.of("{json}"));
    when(reportSQSGateway.emit("{json}")).thenReturn(Mono.empty());
    when(notificacionSQSGateway.emit("{json}")).thenReturn(Mono.empty());

    // Act & Assert
    StepVerifier.create(useCase.updateCreditStatus(id, request, token))
            .assertNext(response -> {
              assert response.getStatusResponse().equals(Constants.STATUS_OK);
              assert response.getCreditParameters().getEstado().equals(Constants.STATUS_APPROVED);
              assert response.getCreditParameters().getEmailNotification().equals("user@mail.com");
            })
            .verifyComplete();

    verify(reportSQSGateway, times(1)).emit("{json}");
    verify(notificacionSQSGateway, times(1)).emit("{json}");
    verify(creditGateway, times(1)).save(any(CreditParameters.class));
  }

  @Test
  void updateCreditStatus_creditFoundAndRejected() {
    Long id = 2L;
    String token = "jwt.token";
    CreditApproved request = new CreditApproved(false);

    CreditParameters params = CreditParameters.builder()
            .monto(BigDecimal.valueOf(5000.0))
            .estado("PENDING")
            .build();

    CreditReponse existing = CreditReponse.builder()
            .creditParameters(params)
            .build();

    when(jwtProvider.getEmailFromToken(token)).thenReturn("user@mail.com");
    when(creditGateway.findById(id)).thenReturn(Mono.just(existing));
    when(creditGateway.save(any(CreditParameters.class)))
            .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
    when(jsonConverter.toJson(any())).thenReturn(Optional.of("{json}"));
    when(notificacionSQSGateway.emit("{json}")).thenReturn(Mono.empty());

    StepVerifier.create(useCase.updateCreditStatus(id, request, token))
            .assertNext(response -> {
              assert response.getCreditParameters().getEstado().equals(Constants.STATUS_REJECTED);
            })
            .verifyComplete();

    verify(reportSQSGateway, never()).emit(any());
    verify(notificacionSQSGateway, times(1)).emit("{json}");
  }

  @Test
  void updateCreditStatus_creditNotFound() {
    Long id = 3L;
    String token = "jwt.token";
    CreditApproved request = new CreditApproved(true);

    when(jwtProvider.getEmailFromToken(token)).thenReturn("user@mail.com");
    when(creditGateway.findById(id)).thenReturn(Mono.empty());

    StepVerifier.create(useCase.updateCreditStatus(id, request, token))
            .assertNext(response -> {
              assert response.getStatusResponse().equals(Constants.STATUS_ERROR);
              assert response.getErrorMessage().contains("no encontrado");
            })
            .verifyComplete();
  }


  @Test
  void updateCreditStatus_gatewayThrowsError() {
    Long id = 4L;
    String token = "jwt.token";
    CreditApproved request = new CreditApproved(true);

    when(jwtProvider.getEmailFromToken(token)).thenReturn("user@mail.com");
    when(creditGateway.findById(id)).thenReturn(Mono.error(new RuntimeException("DB error")));

    StepVerifier.create(useCase.updateCreditStatus(id, request, token))
            .assertNext(response -> {
              assert response.getStatusResponse().equals(Constants.STATUS_ERROR);
              assert response.getErrorMessage().contains("DB error");
            })
            .verifyComplete();
  }

}
