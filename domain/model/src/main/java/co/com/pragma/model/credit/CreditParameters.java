package co.com.pragma.model.credit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreditParameters {
  private Long idEntidadGuardada;
  private Long userId;
  private BigDecimal monto;
  private Integer plazoMeses;
  private Long tipoPrestamo;
  private String estado;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaDecision;
  private String estadoSolicitud;
  private BigDecimal  tasaInteres;
  private Long documentNumber;
  private String emailNotification;
}
