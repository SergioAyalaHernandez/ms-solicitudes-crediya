package co.com.pragma.usecase;

import co.com.pragma.model.credit.CreditDetailDTO;
import co.com.pragma.model.gateway.CreditGateway;
import co.com.pragma.usecase.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreditListUseCaseTest {

  @Mock
  private CreditGateway creditGateway;

  private CreditListUseCase creditListUseCase;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    creditListUseCase = new CreditListUseCase(creditGateway);
  }

  @Test
  void getCreditsList_shouldReturnCreditListResponse_whenSuccessful() {
    // Arrange
    int page = 0;
    int size = 10;
    String token = "test-token";

    List<CreditDetailDTO> creditDetails = Arrays.asList(
            CreditDetailDTO.builder().nombre("1").build(),
            CreditDetailDTO.builder().nombre("2").build()
    );
    Long totalCredits = 2L;

    when(creditGateway.findSizeAllCredits()).thenReturn(Mono.just(totalCredits));
    when(creditGateway.findAllCredits(page, size, token)).thenReturn(Flux.fromIterable(creditDetails));

    // Act & Assert
    StepVerifier.create(creditListUseCase.getCreditsList(page, size, token))
            .expectNextMatches(response ->
                    response.getTotalCredits().equals(totalCredits) &&
                            response.getCreditDetailDTO().size() == 2 &&
                            response.getStatusResponse().equals(Constants.STATUS_OK) &&
                            response.getErrorMessage() == null
            )
            .verifyComplete();

    verify(creditGateway).findSizeAllCredits();
    verify(creditGateway).findAllCredits(page, size, token);
  }

  @Test
  void getCreditsList_shouldReturnErrorResponse_whenGatewayFails() {
    // Arrange
    int page = 0;
    int size = 10;
    String token = "test-token";
    String errorMessage = "Error fetching credits";

    when(creditGateway.findSizeAllCredits()).thenReturn(Mono.just(0L));
    when(creditGateway.findAllCredits(page, size, token)).thenReturn(Flux.error(new RuntimeException(errorMessage)));

    // Act & Assert
    StepVerifier.create(creditListUseCase.getCreditsList(page, size, token))
            .expectNextMatches(response ->
                    response.getTotalCredits() == 0L &&
                            response.getCreditDetailDTO().isEmpty() &&
                            response.getStatusResponse().equals(Constants.STATUS_ERROR) &&
                            response.getErrorMessage().equals(errorMessage)
            )
            .verifyComplete();
  }
}