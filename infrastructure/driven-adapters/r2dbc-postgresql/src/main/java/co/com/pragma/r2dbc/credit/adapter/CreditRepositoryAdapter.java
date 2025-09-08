package co.com.pragma.r2dbc.credit.adapter;

import co.com.pragma.model.credit.CreditDetailDTO;
import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.model.credit.CreditReponse;
import co.com.pragma.model.gateway.CreditGateway;
import co.com.pragma.r2dbc.credit.entity.CreditApplication;
import co.com.pragma.r2dbc.credit.mapper.CreditApplicationMapper;
import co.com.pragma.r2dbc.credit.repository.CreditApplicationRepository;
import co.com.pragma.r2dbc.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Log
public class CreditRepositoryAdapter implements CreditGateway {

  private final CreditApplicationRepository creditApplicationRepository;
  private final CreditApplicationMapper creditApplicationMapper;

  @Override
  public Mono<CreditReponse> createCredit(CreditParameters creditParameters) {
    return Mono.just(creditParameters)
            .map(creditApplicationMapper::toEntity)
            .flatMap(this::saveCreditEntity)
            .onErrorResume(e -> handleCreateCreditError(e, creditParameters));
  }

  private Mono<CreditReponse> saveCreditEntity(CreditApplication entity) {
    log.info(Constants.LOG_ENTITY_BEFORE_SAVE + entity);
    return creditApplicationRepository.save(entity)
            .map(savedEntity -> CreditReponse.builder()
                    .statusResponse(Constants.STATUS_SUCCESS)
                    .creditParameters(creditApplicationMapper.toDto(savedEntity))
                    .build());
  }

  private Mono<CreditReponse> handleCreateCreditError(Throwable e, CreditParameters creditParameters) {
              log.severe(Constants.LOG_ERROR_SAVE + e.getMessage());
              return Mono.just(CreditReponse.builder()
                      .statusResponse(Constants.STATUS_FAILURE)
                      .creditParameters(creditParameters)
                      .errorMessage(e.getMessage())
                      .build());
  }

  @Override
  public Flux<CreditDetailDTO> findAllCredits(int page, int size, String token) {
    return creditApplicationRepository.findAllCreditDetails(createPageable(page, size));
  }

  private Pageable createPageable(int page, int size) {
    return PageRequest.of(page, size);
  }


  @Override
  public Mono<Long> findSizeAllCredits() {
    return creditApplicationRepository.countAllCredits();
  }

  @Override
  public Mono<CreditReponse> findById(Long id) {
      return creditApplicationRepository.findById(id)
              .map(entity -> CreditReponse.builder()
                      .statusResponse(Constants.STATUS_SUCCESS)
                      .creditParameters(creditApplicationMapper.toDto(entity))
                      .build())
              .onErrorResume(e -> {
                  log.severe(Constants.LOG_ERROR_UPDATE + e.getMessage());
                  return Mono.just(CreditReponse.builder()
                          .statusResponse(Constants.STATUS_FAILURE)
                          .errorMessage(e.getMessage())
                          .build());
              });
  }

  @Override
  public Mono<CreditParameters> save(CreditParameters creditParameters) {
    return Mono.just(creditParameters)
            .map(creditApplicationMapper::toEntity)
            .flatMap(creditApplicationRepository::save)
            .map(creditApplicationMapper::toDto);
  }
}
