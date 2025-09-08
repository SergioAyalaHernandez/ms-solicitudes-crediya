package co.com.pragma.model.gateway;

import co.com.pragma.model.credit.CreditDetailDTO;
import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.model.credit.CreditReponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface CreditGateway {
  Mono<CreditReponse> createCredit(CreditParameters creditParameters);
  Flux<CreditDetailDTO> findAllCredits(int page, int size, String token);
  Mono<Long> findSizeAllCredits();
  Mono<CreditReponse> findById(Long id);
  Mono<CreditParameters> save(CreditParameters creditParameters);
}
