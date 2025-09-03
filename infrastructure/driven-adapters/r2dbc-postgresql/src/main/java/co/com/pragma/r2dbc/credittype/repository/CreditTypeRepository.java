package co.com.pragma.r2dbc.credittype.repository;

import co.com.pragma.r2dbc.credittype.entity.CreditType;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CreditTypeRepository extends ReactiveCrudRepository<CreditType, Long> {
}
