package co.com.pragma.consumer;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ObjectResponse {
    private Long id;
    private String nombres;
    private String apellidos;
    private String fechaNacimiento;
    private String direccion;
    private String telefono;
    private String correoElectronico;
    private Double salarioBase;
    private Integer numeroDocumento;
    private Boolean exists;
}