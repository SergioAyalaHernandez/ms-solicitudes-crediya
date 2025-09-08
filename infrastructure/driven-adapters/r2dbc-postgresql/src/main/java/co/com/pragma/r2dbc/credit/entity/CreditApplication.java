package co.com.pragma.r2dbc.credit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("solicitudes_credito")
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreditApplication {

  @Id
  @Column("id")
  private Long id;

  @NotNull(message = "El usuario es obligatorio")
  @Column("user_id")
  private Long userId;

  @NotNull(message = "El monto es obligatorio")
  @Min(value = 100000, message = "El monto mínimo es 100.000")
  @Column("monto")
  private BigDecimal monto;

  @NotNull(message = "El plazo es obligatorio")
  @Min(value = 1, message = "El plazo debe ser al menos 1 mes")
  @Column("plazo_meses")
  private Integer plazoMeses;

  @NotNull(message = "El tipo de préstamo es obligatorio")
  @Column("id_tipo_credito")
  private Long tipoPrestamo;

  @Column("estado")
  private String estado;

  @Column("fecha_creacion")
  private LocalDateTime fechaCreacion;

  @Column("fecha_decision")
  private LocalDateTime fechaDecision;

  @Column("estado_solicitud")
  private String estadoSolicitud;

  @Column("tasa_interes")
  private BigDecimal tasaInteres;
}

