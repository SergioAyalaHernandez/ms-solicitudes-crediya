package co.com.pragma.model.credit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CreditDetailDTO {
  private String nombre;
  private String email;
  private Double monto;
  private Integer plazomeses;
  private String tipocredito;
  private Double tasainteres;
  private Double salariobase;
  private String estadosolicitud;
}


