package co.com.pragma.consumer.utils;

public class Constants {
  private Constants() {}

  // Paths
  public static final String USER_BY_DOCUMENT_PATH = "/api/v1/usuarios/{document}";

  // Mensajes de log
  public static final String LOG_INIT_QUERY = "Iniciando consulta para el documento: ";
  public static final String LOG_SUCCESS_RESPONSE = "Respuesta exitosa para el documento ";
  public static final String LOG_USER_NOT_FOUND = "Usuario no encontrado para el documento ";
  public static final String LOG_HTTP_ERROR = "Error en la consulta HTTP para el documento ";
  public static final String LOG_GENERIC_ERROR = "Error al consultar el usuario con documento ";
  public static final String LOG_RETURNED_ID = "ID retornado para documento ";

  // Errores
  public static final String ERROR_HTTP_PREFIX = "Error HTTP: ";
  public static final String ERROR_404 = "404 Not Found";
}
