package co.com.pragma.r2dbc.credittype.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("TIPOS_CREDITO")
@Data
public class CreditType {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column("id")
  private Long id;

  @NotBlank(message = "El nombre del tipo de crédito es obligatorio")
  @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
  @Column("nombre")
  private String nombre;

  @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
  @Column("descripcion")
  private String descripcion;
}
