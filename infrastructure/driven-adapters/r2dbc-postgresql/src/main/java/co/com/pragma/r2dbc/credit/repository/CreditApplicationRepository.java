package co.com.pragma.r2dbc.credit.repository;

import co.com.pragma.model.credit.CreditDetailDTO;
import co.com.pragma.r2dbc.credit.entity.CreditApplication;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditApplicationRepository extends ReactiveCrudRepository<CreditApplication, Long> {
  @Query("""
              SELECT 
                CONCAT(u.nombres, ' ', u.apellidos) AS nombre,
                u.correo_electronico AS email,
                sc.monto AS monto,
                sc.plazo_meses AS plazomeses,
                tc.nombre AS tipocredito,
                sc.tasa_interes AS tasainteres,
                u.salario_base AS salariobase,
                sc.estado_solicitud AS estadosolicitud
              FROM crediya.solicitudes_credito sc
              LEFT JOIN crediya.tipos_credito tc ON sc.id_tipo_credito = tc.id
              LEFT JOIN crediya.users u ON sc.user_id = u.id
              LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
          """)
  Flux<CreditDetailDTO> findAllCreditDetails(Pageable pageable);

  @Query("SELECT COUNT(*) FROM crediya.solicitudes_credito")
  Mono<Long> countAllCredits();

  Flux<CreditApplication> findAllByUserId(Long userId);
}