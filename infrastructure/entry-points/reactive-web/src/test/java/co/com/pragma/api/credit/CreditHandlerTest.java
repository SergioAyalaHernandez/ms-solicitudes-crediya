package co.com.pragma.api.credit;

import co.com.pragma.api.utils.Constants;
import co.com.pragma.model.credit.CreditApproved;
import co.com.pragma.model.credit.CreditDetailDTO;
import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.model.credit.CreditReponse;
import co.com.pragma.usecase.CalculateDebtCapacityUseCase;
import co.com.pragma.usecase.CreateCreditUseCase;
import co.com.pragma.usecase.CreditListUseCase;
import co.com.pragma.usecase.UpdateCreditUseCase;
import co.com.pragma.usecase.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditHandlerTest {

  @Mock
  private CreateCreditUseCase createCreditUseCase;

  @Mock
  private CreditListUseCase creditListUseCase;

  @Mock
  private UpdateCreditUseCase updateCreditUSeCase;

  @Mock
  private CalculateDebtCapacityUseCase calculateDebtCapacityUseCase;

  @Mock
  private ServerRequest serverRequest;

  private CreditHandler creditHandler;

  @BeforeEach
  void setUp() {
    // Arrange
    creditHandler = new CreditHandler(createCreditUseCase, creditListUseCase, updateCreditUSeCase, calculateDebtCapacityUseCase);
  }

  @Test
  void createCredit_Success() {
    // Arrange
    String token = "Bearer token";
    CreditParameters creditParameters = new CreditParameters();
    CreditReponse expectedResponse = new CreditReponse();

    ServerRequest.Headers headers = Mockito.mock(ServerRequest.Headers.class);
    when(headers.firstHeader(Constants.HEADER_AUTHORIZATION)).thenReturn(token);
    when(serverRequest.headers()).thenReturn(headers);
    when(serverRequest.bodyToMono(CreditParameters.class)).thenReturn(Mono.just(creditParameters));
    when(createCreditUseCase.createCredit(eq(creditParameters), eq(token))).thenReturn(Mono.just(expectedResponse));

    // Act
    Mono<ServerResponse> result = creditHandler.createCredit(serverRequest);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
            .verifyComplete();
  }

  @Test
  void createCredit_Error() {
    // Arrange
    String token = "Bearer token";
    CreditParameters creditParameters = new CreditParameters();
    String errorMessage = "Error creating credit";

    ServerRequest.Headers headers = Mockito.mock(ServerRequest.Headers.class);
    when(headers.firstHeader(Constants.HEADER_AUTHORIZATION)).thenReturn(String.valueOf(Optional.of(token)));
    when(serverRequest.headers()).thenReturn(headers);
    when(serverRequest.bodyToMono(CreditParameters.class)).thenReturn(Mono.just(creditParameters));
    when(createCreditUseCase.createCredit(eq(creditParameters), eq(token))).thenReturn(Mono.error(new RuntimeException(errorMessage)));

    // Act
    Mono<ServerResponse> result = creditHandler.createCredit(serverRequest);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response -> response.statusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR))
            .verifyComplete();
  }

  @Test
  void getCreditsList_Success() {
    // Arrange
    String token = "Bearer token";
    int page = 0;
    int size = 10;
    CreditDetailDTO credit1 = new CreditDetailDTO();

    ServerRequest.Headers headers = Mockito.mock(ServerRequest.Headers.class);
    when(headers.firstHeader(Constants.HEADER_AUTHORIZATION)).thenReturn(token);
    when(serverRequest.headers()).thenReturn(headers);
    when(serverRequest.queryParam(Constants.PAGE)).thenReturn(Optional.of(String.valueOf(page)));
    when(serverRequest.queryParam(Constants.SIZE)).thenReturn(Optional.of(String.valueOf(size)));
    Mockito.doReturn(Flux.just(credit1)).when(creditListUseCase).getCreditsList(eq(page), eq(size), eq(token));

    // Act
    Mono<ServerResponse> result = creditHandler.getCreditsList(serverRequest);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response -> {
              response.statusCode().is2xxSuccessful();
              return true;
            })
            .verifyComplete();
  }

  @Test
  void getCreditsList_Error() {
    // Arrange
    String token = "Bearer token";
    int page = 0;
    int size = 10;
    String errorMessage = "Error getting credits";

    ServerRequest.Headers headers = Mockito.mock(ServerRequest.Headers.class);
    when(headers.firstHeader(Constants.HEADER_AUTHORIZATION)).thenReturn(token);
    when(serverRequest.headers()).thenReturn(headers);
    when(serverRequest.queryParam(Constants.PAGE)).thenReturn(Optional.of(String.valueOf(page)));
    when(serverRequest.queryParam(Constants.SIZE)).thenReturn(Optional.of(String.valueOf(size)));
    when(creditListUseCase.getCreditsList(eq(page), eq(size), eq(token))).thenReturn(Flux.error(new RuntimeException(errorMessage)));

    // Act
    Mono<ServerResponse> result = creditHandler.getCreditsList(serverRequest);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response -> response.statusCode().is5xxServerError());
  }

  @Test
  void getCreditsList_DefaultValues() {
    // Arrange
    String token = "Bearer token";
    int defaultPage = Integer.parseInt(Constants.DEFAULT_PAGE);
    int defaultSize = Integer.parseInt(Constants.DEFAULT_SIZE);

    ServerRequest.Headers headers = Mockito.mock(ServerRequest.Headers.class);
    when(headers.firstHeader(Constants.HEADER_AUTHORIZATION)).thenReturn(token);
    when(serverRequest.headers()).thenReturn(headers);
    when(serverRequest.queryParam(Constants.PAGE)).thenReturn(Optional.empty());
    when(serverRequest.queryParam(Constants.SIZE)).thenReturn(Optional.empty());
    when(creditListUseCase.getCreditsList(eq(defaultPage), eq(defaultSize), eq(token))).thenReturn(Flux.empty());

    // Act
    Mono<ServerResponse> result = creditHandler.getCreditsList(serverRequest);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
            .verifyComplete();
  }

  // AAA: Arrange, Act, Assert
  @Test
  void getCreditsList_Error_500() {
    // Arrange
    String token = "Bearer token";
    int page = 0;
    int size = 10;
    String errorMessage = "Error al obtener créditos";

    ServerRequest.Headers headers = Mockito.mock(ServerRequest.Headers.class);
    when(headers.firstHeader(Constants.HEADER_AUTHORIZATION)).thenReturn(token);
    when(serverRequest.headers()).thenReturn(headers);
    when(serverRequest.queryParam(Constants.PAGE)).thenReturn(Optional.of(String.valueOf(page)));
    when(serverRequest.queryParam(Constants.SIZE)).thenReturn(Optional.of(String.valueOf(size)));
    when(creditListUseCase.getCreditsList(eq(page), eq(size), eq(token)))
            .thenReturn(Flux.error(new RuntimeException(errorMessage)));

    // Act
    Mono<ServerResponse> result = creditHandler.getCreditsList(serverRequest);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response -> response.statusCode().is5xxServerError())
            .verifyComplete();
  }

  @Test
  void updateCredit_Success() {
    // Arrange
    Long id = 1L;
    CreditApproved creditApproved = new CreditApproved();
    CreditReponse expectedResponse = new CreditReponse();

    when(serverRequest.pathVariable(Constants.ID)).thenReturn(String.valueOf(id));
    when(serverRequest.bodyToMono(CreditApproved.class)).thenReturn(Mono.just(creditApproved));
    when(updateCreditUSeCase.updateCreditStatus(eq(id), eq(creditApproved),"Bearer token")).thenReturn(Mono.just(expectedResponse));

    // Act
    Mono<ServerResponse> result = creditHandler.updateCredit(serverRequest);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
            .verifyComplete();
  }

  @Test
  void updateCredit_NotFound() {
    // Arrange
    Long id = 1L;
    CreditApproved creditApproved = new CreditApproved();
    String errorMessage = "Credit not found";

    when(serverRequest.pathVariable(Constants.ID)).thenReturn(String.valueOf(id));
    when(serverRequest.bodyToMono(CreditApproved.class)).thenReturn(Mono.just(creditApproved));
    when(updateCreditUSeCase.updateCreditStatus(eq(id), eq(creditApproved),"Bearer token"))
            .thenReturn(Mono.error(new NotFoundException(errorMessage)));

    // Act
    Mono<ServerResponse> result = creditHandler.updateCredit(serverRequest);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response -> response.statusCode().equals(HttpStatus.NOT_FOUND))
            .verifyComplete();
  }

  @Test
  void updateCredit_Error() {
    // Arrange
    Long id = 1L;
    CreditApproved creditApproved = new CreditApproved();
    String errorMessage = "Internal server error";

    when(serverRequest.pathVariable(Constants.ID)).thenReturn(String.valueOf(id));
    when(serverRequest.bodyToMono(CreditApproved.class)).thenReturn(Mono.just(creditApproved));
    when(updateCreditUSeCase.updateCreditStatus(eq(id), eq(creditApproved),"Bearer token"))
            .thenReturn(Mono.error(new RuntimeException(errorMessage)));

    // Act
    Mono<ServerResponse> result = creditHandler.updateCredit(serverRequest);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response -> response.statusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR))
            .verifyComplete();
  }
}