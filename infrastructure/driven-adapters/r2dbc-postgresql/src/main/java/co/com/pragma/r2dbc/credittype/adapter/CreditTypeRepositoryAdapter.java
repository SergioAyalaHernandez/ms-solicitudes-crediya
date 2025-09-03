package co.com.pragma.r2dbc.credittype.adapter;

import co.com.pragma.model.gateway.CreditTypeGateway;
import co.com.pragma.r2dbc.credittype.entity.CreditType;
import co.com.pragma.r2dbc.credittype.repository.CreditTypeRepository;
import co.com.pragma.r2dbc.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Log
public class CreditTypeRepositoryAdapter implements CreditTypeGateway {
  private final CreditTypeRepository creditTypeRepository;

  @Override
  public Mono<Long> getCreditTypeById(Long id) {
    log.info(String.format(Constants.LOG_QUERY_CREDIT_TYPE, id));
    return creditTypeRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException(
                    String.format(Constants.ERROR_CREDIT_TYPE_NOT_FOUND, id))))
            .map(CreditType::getId)
            .doOnSuccess(foundId -> log.info(String.format(Constants.LOG_CREDIT_TYPE_FOUND, foundId)))
            .doOnError(error -> log.warning(String.format(Constants.LOG_ERROR_QUERY_CREDIT_TYPE, error.getMessage())));
  }
}
