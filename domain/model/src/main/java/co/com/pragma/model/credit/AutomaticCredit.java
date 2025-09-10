package co.com.pragma.model.credit;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class AutomaticCredit {
  private String solicitudId;
  private String tipoPrestamoId;
  private BigDecimal montoSolicitado;
  private int plazoMeses;
  private BigDecimal tasaInteresMensual;
  private BigDecimal ingresosTotales;
  private List<PrestamoActivo> prestamosActivos;
  private String correoSolicitante;
  private LocalDateTime fechaSolicitud;
}
