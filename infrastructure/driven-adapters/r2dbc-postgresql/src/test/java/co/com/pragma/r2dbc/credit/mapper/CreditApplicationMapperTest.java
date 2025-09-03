package co.com.pragma.r2dbc.credit.mapper;

import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.r2dbc.credit.entity.CreditApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CreditApplicationMapperTest {

  private CreditApplicationMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(CreditApplicationMapper.class);
  }
  @Test
  void shouldMapToCreditParameters() {
    // Arrange
    CreditApplication entity = CreditApplication.builder()
        .id(1L)
        .userId(123L)
        .monto(BigDecimal.valueOf(1000000))
        .plazoMeses(12)
        .tipoPrestamo(2L)
        .estado("NUEVO")
        .fechaCreacion(LocalDateTime.now())
        .fechaDecision(null)
        .estadoSolicitud("PENDIENTE")
        .tasaInteres(BigDecimal.valueOf(0.15))
        .build();

    // Act
    CreditParameters model = mapper.toDto(entity);

    // Assert
    assertEquals(entity.getUserId(), model.getUserId());
    assertEquals(entity.getMonto(), model.getMonto());
    assertEquals(entity.getPlazoMeses(), model.getPlazoMeses());
    assertEquals(entity.getTipoPrestamo(), model.getTipoPrestamo());
    assertEquals(entity.getEstado(), model.getEstado());
    assertEquals(entity.getFechaCreacion(), model.getFechaCreacion());
    assertEquals(entity.getFechaDecision(), model.getFechaDecision());
    assertEquals(entity.getEstadoSolicitud(), model.getEstadoSolicitud());
    assertEquals(entity.getTasaInteres(), model.getTasaInteres());
    assertNull(model.getDocumentNumber());
  }

  @Test
  void shouldMapToCreditApplication() {
    // Arrange
    CreditParameters model = CreditParameters.builder()
        .userId(123L)
        .monto(BigDecimal.valueOf(1000000))
        .plazoMeses(12)
        .tipoPrestamo(2L)
        .estado("NUEVO")
        .fechaCreacion(LocalDateTime.now())
        .fechaDecision(null)
        .estadoSolicitud("PENDIENTE")
        .tasaInteres(BigDecimal.valueOf(0.15))
        .documentNumber(1234567890L)
        .build();

    // Act
    CreditApplication entity = mapper.toEntity(model);

    // Assert
    assertNull(entity.getId());
    assertEquals(model.getUserId(), entity.getUserId());
    assertEquals(model.getMonto(), entity.getMonto());
    assertEquals(model.getPlazoMeses(), entity.getPlazoMeses());
    assertEquals(model.getTipoPrestamo(), entity.getTipoPrestamo());
    assertEquals(model.getEstado(), entity.getEstado());
    assertEquals(model.getFechaCreacion(), entity.getFechaCreacion());
    assertEquals(model.getFechaDecision(), entity.getFechaDecision());
    assertEquals(model.getEstadoSolicitud(), entity.getEstadoSolicitud());
    assertEquals(model.getTasaInteres(), entity.getTasaInteres());
  }
}