package co.com.pragma.r2dbc.utils;

public class Constants {
  private Constants() {
  }

  // Estados de respuesta
  public static final String STATUS_SUCCESS = "SUCCESS";
  public static final String STATUS_FAILURE = "FAILURE";

  // Mensajes de log
  public static final String LOG_ENTITY_BEFORE_SAVE = "Entidad creada antes de guardar: ";
  public static final String LOG_ERROR_SAVE = "Error al guardar el crédito: ";
  public static final String LOG_ERROR_UPDATE = "Error al actualizar el crédito: ";

  // credit type
  public static final String ERROR_CREDIT_TYPE_NOT_FOUND = "Tipo de crédito con ID %d no encontrado";
  public static final String LOG_QUERY_CREDIT_TYPE = "Consultando tipo de crédito con ID: %d";
  public static final String LOG_CREDIT_TYPE_FOUND = "Tipo de crédito encontrado con ID: %d";
  public static final String LOG_ERROR_QUERY_CREDIT_TYPE = "Error al buscar tipo de crédito: %s";
}
