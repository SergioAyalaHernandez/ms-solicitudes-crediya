package co.com.pragma.r2dbc.credit.mapper;

import co.com.pragma.model.credit.CreditParameters;
import co.com.pragma.r2dbc.credit.entity.CreditApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreditApplicationMapper {

  @Mapping(target = "id", ignore = true)
  CreditApplication toEntity(CreditParameters dto);

  CreditParameters toDto(CreditApplication entity);

}