package co.com.pragma.r2dbc.credit.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditListDto {
  @Column(name = "nombreCompleto")
  private String nombreCompleto;

  @Column(name = "email")
  private String email;

  @Column(name = "monto")
  private Double monto;

  @Column(name = "plazoMeses")
  private Integer plazoMeses;

  @Column(name = "tipoCredito")
  private String tipoCredito;

  @Column(name = "tasaInteres")
  private Double tasaInteres;

  @Column(name = "salarioBase")
  private Double salarioBase;

  @Column(name = "estadoSolicitud")
  private String estadoSolicitud;

}

