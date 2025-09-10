package co.com.pragma.model.credit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class PrestamoActivo {
  private String prestamoId;
  private double monto;
  private int plazoMeses;
  private double tasaInteresMensual;
}