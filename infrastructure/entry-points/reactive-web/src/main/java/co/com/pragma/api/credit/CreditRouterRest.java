package co.com.pragma.api.credit;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Component
@RequiredArgsConstructor
public class CreditRouterRest {

  @Bean
  @RouterOperations({
          @RouterOperation(
                  path = "/api/v1/solicitud",
                  beanClass = CreditHandler.class,
                  beanMethod = "createCredit",
                  operation = @Operation(
                          operationId = "createCredit",
                          summary = "Crear una nueva solicitud de crédito",
                          description = "Crea una nueva solicitud de crédito con los parámetros proporcionados",
                          requestBody = @RequestBody(
                                  required = true,
                                  content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                          schema = @Schema(implementation = Object.class))
                          ),
                          responses = {
                                  @ApiResponse(
                                          responseCode = "200",
                                          description = "Solicitud creada correctamente",
                                          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                                  ),
                                  @ApiResponse(
                                          responseCode = "400",
                                          description = "Parámetros incorrectos"
                                  )
                          }
                  )
          ),
          @RouterOperation(
                  path = "/api/v1/solicitudes",
                  beanClass = CreditHandler.class,
                  beanMethod = "getCreditsList",
                  operation = @Operation(
                          operationId = "getCreditsList",
                          summary = "Obtener lista de solicitudes de crédito",
                          description = "Retorna una lista paginada de solicitudes de crédito",
                          parameters = {
                                  @Parameter(
                                          in = ParameterIn.QUERY,
                                          name = "page",
                                          description = "Número de página (comenzando desde 0)",
                                          schema = @Schema(implementation = Integer.class, defaultValue = "0")
                                  ),
                                  @Parameter(
                                          in = ParameterIn.QUERY,
                                          name = "size",
                                          description = "Tamaño de la página",
                                          schema = @Schema(implementation = Integer.class, defaultValue = "6")
                                  )
                          },
                          responses = {
                                  @ApiResponse(
                                          responseCode = "200",
                                          description = "Lista de solicitudes obtenida correctamente",
                                          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                                  ),
                                  @ApiResponse(
                                          responseCode = "403",
                                          description = "Acceso denegado"
                                  )
                          }
                  )
          ),
          @RouterOperation(
                  path = "/api/v1/solicitud/{id}",
                  beanClass = CreditHandler.class,
                  beanMethod = "updateCredit",
                  operation = @Operation(
                          operationId = "updateCredit",
                          summary = "Actualizar una solicitud de crédito",
                          description = "Actualiza la solicitud de crédito por ID",
                          parameters = {
                                  @Parameter(
                                          in = ParameterIn.PATH,
                                          name = "id",
                                          description = "ID de la solicitud a actualizar",
                                          required = true,
                                          schema = @Schema(implementation = Long.class)
                                  )
                          },
                          requestBody = @RequestBody(
                                  required = true,
                                  content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                          schema = @Schema(implementation = Object.class))
                          ),
                          responses = {
                                  @ApiResponse(
                                          responseCode = "200",
                                          description = "Solicitud actualizada correctamente",
                                          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                                  ),
                                  @ApiResponse(
                                          responseCode = "404",
                                          description = "Solicitud no encontrada"
                                  )
                          }
                  )
          )
  })
  public RouterFunction<ServerResponse> routerFunction(CreditHandler handler) {
    return route(POST("/api/v1/solicitud"), handler::createCredit)
            .andRoute(GET("/api/v1/solicitudes"), handler::getCreditsList)
            .andRoute(PUT("/api/v1/solicitud/{id}"), handler::updateCredit);
  }
}

