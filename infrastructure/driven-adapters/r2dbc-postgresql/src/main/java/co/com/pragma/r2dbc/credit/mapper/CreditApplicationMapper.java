package co.com.pragma.r2dbc.credit.mapper;

import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.r2dbc.credit.entity.CreditApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditApplicationMapper {
  @Mapping(source = "idEntidadGuardada", target = "id")
  CreditApplication toEntity(CreditParameters dto);

  @Mapping(source = "id", target = "idEntidadGuardada")
  CreditParameters toDto(CreditApplication entity);

}