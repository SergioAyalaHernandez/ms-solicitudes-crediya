package co.com.pragma.r2dbc.credittype.adapter;

import co.com.pragma.r2dbc.credittype.entity.CreditType;
import co.com.pragma.r2dbc.credittype.repository.CreditTypeRepository;
import co.com.pragma.r2dbc.utils.Constants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditTypeRepositoryAdapterTest {

  @Mock
  private CreditTypeRepository creditTypeRepository;

  @InjectMocks
  private CreditTypeRepositoryAdapter creditTypeRepositoryAdapter;

  @Test
  @DisplayName("Debe retornar el ID del tipo de crédito cuando existe")
  void getCreditTypeById_WhenCreditTypeExists_ShouldReturnId() {
    // Arrange
    Long expectedId = 1L;
    CreditType creditType = new CreditType();
    creditType.setId(expectedId);

    when(creditTypeRepository.findById(expectedId)).thenReturn(Mono.just(creditType));

    // Act
    Mono<Long> result = creditTypeRepositoryAdapter.getCreditTypeById(expectedId);

    // Assert
    StepVerifier.create(result)
            .expectNext(expectedId)
            .verifyComplete();

    verify(creditTypeRepository).findById(expectedId);
  }

  @Test
  @DisplayName("Debe lanzar un error cuando el tipo de crédito no existe")
  void getCreditTypeById_WhenCreditTypeDoesNotExist_ShouldThrowError() {
    // Arrange
    Long nonExistentId = 999L;
    String expectedErrorMessage = String.format(Constants.ERROR_CREDIT_TYPE_NOT_FOUND, nonExistentId);

    when(creditTypeRepository.findById(nonExistentId)).thenReturn(Mono.empty());

    // Act
    Mono<Long> result = creditTypeRepositoryAdapter.getCreditTypeById(nonExistentId);

    // Assert
    StepVerifier.create(result)
            .expectErrorMatches(error ->
                    error instanceof RuntimeException &&
                            error.getMessage().equals(expectedErrorMessage))
            .verify();

    verify(creditTypeRepository).findById(nonExistentId);
  }
}