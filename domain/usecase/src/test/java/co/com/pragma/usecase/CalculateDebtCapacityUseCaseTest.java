package co.com.pragma.usecase;

import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.model.credit.CreditReponse;
import co.com.pragma.model.credit.PrestamoActivo;
import co.com.pragma.model.gateway.*;
import co.com.pragma.usecase.exceptions.ConstraintViolationException;
import co.com.pragma.usecase.gateway.GatewayExposeUser;
import co.com.pragma.usecase.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CalculateDebtCapacityUseCaseTest {

  private CreditGateway creditGateway;
  private GatewayExposeUser gatewayExposeUser;
  private NotificacionSQSCapacidadGateway notificacionSQSCapacidadGateway;
  private JsonConverter jsonConverter;
  private JwtProvider jwtProvider;
  private CreditTypeGateway creditTypeGateway;
  private CalculateDebtCapacityUseCase useCase;

  @BeforeEach
  void setUp() {
    creditGateway = Mockito.mock(CreditGateway.class);
    gatewayExposeUser = Mockito.mock(GatewayExposeUser.class);
    notificacionSQSCapacidadGateway = Mockito.mock(NotificacionSQSCapacidadGateway.class);
    jsonConverter = Mockito.mock(JsonConverter.class);
    jwtProvider = Mockito.mock(JwtProvider.class);
    creditTypeGateway = Mockito.mock(CreditTypeGateway.class);
    useCase = new CalculateDebtCapacityUseCase(
            creditGateway, gatewayExposeUser, notificacionSQSCapacidadGateway, jsonConverter, jwtProvider, creditTypeGateway
    );
  }

  @Test
  void testCreateCreditAutomaticSuccess() {
    CreditParameters params = CreditParameters.builder()
            .userId(1L)
            .tipoPrestamo(2L)
            .monto(new BigDecimal("1000"))
            .plazoMeses(12)
            .tasaInteres(new BigDecimal("1.5"))
            .documentNumber(Long.valueOf("123456"))
            .build();

    String token = "token";
    Mockito.when(jwtProvider.getEmailFromToken(token)).thenReturn("test@email.com");
    Mockito.when(jwtProvider.getSalarioFromToken(token)).thenReturn(5000.0);
    Mockito.when(jwtProvider.getUserIdFromToken(token)).thenReturn("1");
    Mockito.when(creditTypeGateway.getCreditTypeById(2L)).thenReturn(Mono.just(2L));
    Mockito.when(gatewayExposeUser.findByDocument(Long.valueOf("123456"))).thenReturn(Mono.just(1L));
    Mockito.when(creditGateway.createCredit(Mockito.any())).thenReturn(Mono.just(
            CreditReponse.builder().statusResponse(Constants.STATUS_VALID_USER).creditParameters(params).build()
    ));
    Mockito.when(creditGateway.findAllCredits("1")).thenReturn(reactor.core.publisher.Flux.empty());
    Mockito.when(jsonConverter.toJson(Mockito.any())).thenReturn(Optional.of("{}"));
    Mockito.when(notificacionSQSCapacidadGateway.emit(Mockito.anyString())).thenReturn(Mono.empty());

    Mono<CreditReponse> result = useCase.createCreditAutomatic(params, token);

    CreditReponse response = result.block();
    assertNotNull(response);
    assertEquals(Constants.STATUS_VALID_USER, response.getStatusResponse());
  }

  @Test
  void testCreateCreditAutomaticUserIdMismatch() {
    CreditParameters params = CreditParameters.builder()
            .userId(2L)
            .documentNumber(Long.valueOf("123456"))
            .build();

    String token = "token";
    Mockito.when(jwtProvider.getUserIdFromToken(token)).thenReturn("1");

    Mono<CreditReponse> result = useCase.createCreditAutomatic(params, token);

    CreditReponse response = result.block();
    assertNotNull(response);
    assertEquals(Constants.STATUS_ERROR, response.getStatusResponse());
    assertEquals("Error inesperado", response.getErrorMessage());
  }

  @Test
  void testCreateCreditAutomaticUserNotFound() {
    CreditParameters params = CreditParameters.builder()
            .userId(1L)
            .tipoPrestamo(2L)
            .documentNumber(Long.valueOf("123456"))
            .build();

    String token = "token";
    Mockito.when(jwtProvider.getUserIdFromToken(token)).thenReturn("1");
    Mockito.when(creditTypeGateway.getCreditTypeById(2L)).thenReturn(Mono.just(2L));
    Mockito.when(gatewayExposeUser.findByDocument(Long.valueOf("123456"))).thenReturn(Mono.just(0L));

    Mono<CreditReponse> result = useCase.createCreditAutomatic(params, token);

    CreditReponse response = result.block();
    assertNotNull(response);
    assertEquals(Constants.STATUS_ERROR, response.getStatusResponse());
    assertEquals("Error inesperado", response.getErrorMessage());
  }

  @Test
  void testHandleErrorConstraintViolationException() {
    ConstraintViolationException exception = new ConstraintViolationException(List.of());
    Mono<CreditReponse> result = useCase.createCreditAutomatic(
            CreditParameters.builder().userId(1L).build(), "token"
    ).onErrorResume(e -> useCase.handleError(exception));

    CreditReponse response = result.block();
    assertNotNull(response);
    assertEquals(Constants.STATUS_ERROR, response.getStatusResponse());
    assertEquals(Constants.MSG_UNEXPECTED_ERROR, response.getErrorMessage());
  }

  @Test
  void testSetEmailNotificationIsCalled() {
    CreditParameters params = CreditParameters.builder()
            .userId(1L)
            .tipoPrestamo(2L)
            .documentNumber(123456L)
            .build();
    String token = "token";

    Mockito.when(jwtProvider.getEmailFromToken(token)).thenReturn("test@email.com");
    Mockito.when(jwtProvider.getUserIdFromToken(token)).thenReturn("1");
    Mockito.when(creditTypeGateway.getCreditTypeById(Mockito.any()))
            .thenReturn(Mono.just(1L));
    Mockito.when(gatewayExposeUser.findByDocument(Mockito.any()))
            .thenReturn(Mono.just(1L));
    Mockito.when(creditGateway.createCredit(Mockito.any())).thenAnswer(invocation -> {
      CreditParameters inputParams = invocation.getArgument(0);
      CreditParameters paramsWithEmail = CreditParameters.builder()
              .userId(inputParams.getUserId())
              .documentNumber(inputParams.getDocumentNumber())
              .emailNotification("test@email.com")
              .build();
      return Mono.just(
              CreditReponse.builder()
                      .statusResponse(Constants.STATUS_VALID_USER)
                      .creditParameters(paramsWithEmail)
                      .build()
      );
    });

    Mono<CreditReponse> result = useCase.createCreditAutomatic(params, token);
    CreditReponse response = result.block();

    assertEquals(null, response.getCreditParameters().getEmailNotification());
  }

  @Test
  void testObtenerPrestamosActivos_AAA() {
    // Arrange
    Long userId = 1L;
    var creditMock = Mockito.mock(CreditParameters.class);
    Mockito.when(creditMock.getEstado()).thenReturn(Constants.STATUS_APPROVED);
    Mockito.when(creditMock.getIdEntidadGuardada()).thenReturn(10L);
    Mockito.when(creditMock.getMonto()).thenReturn(new BigDecimal("5000"));
    Mockito.when(creditMock.getPlazoMeses()).thenReturn(24);
    Mockito.when(creditMock.getTasaInteres()).thenReturn(new BigDecimal("2.5"));

    Mockito.when(creditGateway.findAllCredits("1"))
            .thenReturn(reactor.core.publisher.Flux.just(creditMock));

    // Act
    Mono<List<PrestamoActivo>> result = useCase.obtenerPrestamosActivos(userId);

    // Assert
    List<PrestamoActivo> prestamos = result.block();
    assertNotNull(prestamos);
    assertEquals(1, prestamos.size());
    PrestamoActivo prestamo = prestamos.get(0);
    assertEquals("10", prestamo.getPrestamoId());
    assertEquals(5000.0, prestamo.getMonto());
    assertEquals(24, prestamo.getPlazoMeses());
    assertEquals(2.5, prestamo.getTasaInteresMensual());
  }


  @Test
  void testValidateJwtIdUserSetsErrorMessage() {
    CreditParameters params = CreditParameters.builder()
            .userId(2L)
            .build();
    String token = "token";
    Mockito.when(jwtProvider.getUserIdFromToken(token)).thenReturn("1");

    Mono<CreditReponse> result = useCase.validateJwtIdUser(params, token);
    CreditReponse response = result.block();

    assertNotNull(response);
    assertEquals(Constants.MSG_UNAUTHORIZED_USER, response.getErrorMessage());
    assertEquals(Constants.STATUS_ERROR, response.getStatusResponse());
  }

  @Test
  void testExtractErrorMessageConstraintViolationException() {
    var violation1 = Mockito.mock(co.com.pragma.usecase.exceptions.ConstraintViolation.class);
    var violation2 = Mockito.mock(co.com.pragma.usecase.exceptions.ConstraintViolation.class);
    Mockito.when(violation1.getMessage()).thenReturn("Error 1");
    Mockito.when(violation2.getMessage()).thenReturn("Error 2");
    ConstraintViolationException exception = new ConstraintViolationException(List.of(violation1, violation2));

    String result = useCase.extractErrorMessage(exception);

    assertEquals("Error 1, Error 2", result);
  }

  @Test
  void testExtractErrorMessageOtherException() {
    Exception exception = new Exception("Test");
    String result = useCase.extractErrorMessage(exception);
    assertEquals(Constants.MSG_UNEXPECTED_ERROR, result);
  }
}