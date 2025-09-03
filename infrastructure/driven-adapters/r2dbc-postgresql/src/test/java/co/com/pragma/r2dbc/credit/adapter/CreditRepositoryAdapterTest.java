package co.com.pragma.r2dbc.credit.adapter;

import co.com.pragma.model.credit.CreditDetailDTO;
import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.model.credit.CreditReponse;
import co.com.pragma.r2dbc.credit.entity.CreditApplication;
import co.com.pragma.r2dbc.credit.mapper.CreditApplicationMapper;
import co.com.pragma.r2dbc.credit.repository.CreditApplicationRepository;
import co.com.pragma.r2dbc.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditRepositoryAdapterTest {

  @Mock
  private CreditApplicationRepository creditApplicationRepository;

  @Mock
  private CreditApplicationMapper creditApplicationMapper;

  @InjectMocks
  private CreditRepositoryAdapter creditRepositoryAdapter;

  private CreditParameters creditParameters;
  private CreditApplication creditApplication;
  private CreditApplication savedCreditApplication;

  @BeforeEach
  void setUp() {
    creditParameters = CreditParameters.builder()
            .documentNumber(109876543L)
            .monto(new BigDecimal("500000.00"))
            .plazoMeses(24)
            .tipoPrestamo(1L)
            .estado("PENDIENTE")
            .fechaCreacion(LocalDateTime.parse("2023-10-25T10:30:00"))
            .fechaDecision(LocalDateTime.parse("2023-10-26T10:30:00"))
            .estadoSolicitud("EN_REVISION")
            .tasaInteres(new BigDecimal("12.5"))
            .build();

    creditApplication = CreditApplication.builder()
            .monto(new BigDecimal("500000.00"))
            .plazoMeses(24)
            .tipoPrestamo(1L)
            .estado("PENDIENTE")
            .fechaCreacion(LocalDateTime.parse("2023-10-25T10:30:00"))
            .fechaDecision(LocalDateTime.parse("2023-10-26T10:30:00"))
            .tasaInteres(new BigDecimal("12.5"))
            .build();

    savedCreditApplication = CreditApplication.builder()
            .id(1L)
            .monto(new BigDecimal("500000.00"))
            .plazoMeses(24)
            .tipoPrestamo(1L)
            .estado("PENDIENTE")
            .fechaCreacion(LocalDateTime.parse("2023-10-25T10:30:00"))
            .fechaDecision(LocalDateTime.parse("2023-10-26T10:30:00"))
            .tasaInteres(new BigDecimal("12.5"))
            .build();
  }

  @Test
  void shouldCreateCreditSuccessfully() {
    // Arrange
    when(creditApplicationMapper.toEntity(creditParameters)).thenReturn(creditApplication);
    when(creditApplicationRepository.save(creditApplication)).thenReturn(Mono.just(savedCreditApplication));
    when(creditApplicationMapper.toDto(savedCreditApplication)).thenReturn(creditParameters);

    // Act
    Mono<CreditReponse> result = creditRepositoryAdapter.createCredit(creditParameters);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response ->
                    Constants.STATUS_SUCCESS.equals(response.getStatusResponse()) &&
                            response.getCreditParameters() != null &&
                            response.getErrorMessage() == null)
            .verifyComplete();

    verify(creditApplicationMapper).toEntity(creditParameters);
    verify(creditApplicationRepository).save(creditApplication);
    verify(creditApplicationMapper).toDto(savedCreditApplication);
  }

  @Test
  void shouldHandleErrorWhenCreatingCredit() {
    // Arrange
    RuntimeException exception = new RuntimeException("Error al guardar");
    when(creditApplicationMapper.toEntity(creditParameters)).thenReturn(creditApplication);
    when(creditApplicationRepository.save(creditApplication)).thenReturn(Mono.error(exception));

    // Act
    Mono<CreditReponse> result = creditRepositoryAdapter.createCredit(creditParameters);

    // Assert
    StepVerifier.create(result)
            .expectNextMatches(response ->
                    Constants.STATUS_FAILURE.equals(response.getStatusResponse()) &&
                            response.getCreditParameters() != null &&
                            "Error al guardar".equals(response.getErrorMessage()))
            .verifyComplete();

    verify(creditApplicationMapper).toEntity(creditParameters);
    verify(creditApplicationRepository).save(creditApplication);
  }

  @Test
  void shouldFindAllCreditsWithPagination() {
    // Arrange
    int page = 0;
    int size = 10;
    String token = "test-token";
    Pageable pageable = PageRequest.of(page, size);
    CreditDetailDTO creditDetail = CreditDetailDTO.builder().build();

    when(creditApplicationRepository.findAllCreditDetails(pageable))
            .thenReturn(Flux.just(creditDetail));

    // Act
    Flux<CreditDetailDTO> result = creditRepositoryAdapter.findAllCredits(page, size, token);

    // Assert
    StepVerifier.create(result)
            .expectNext(creditDetail)
            .verifyComplete();

    verify(creditApplicationRepository).findAllCreditDetails(pageable);
  }

  @Test
  void shouldFindSizeAllCredits() {
    // Arrange
    when(creditApplicationRepository.countAllCredits()).thenReturn(Mono.just(5L));

    // Act
    Mono<Long> result = creditRepositoryAdapter.findSizeAllCredits();

    // Assert
    StepVerifier.create(result)
            .expectNext(5L)
            .verifyComplete();

    verify(creditApplicationRepository).countAllCredits();
  }
}