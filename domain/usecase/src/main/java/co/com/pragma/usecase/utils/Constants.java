package co.com.pragma.usecase.utils;

public class Constants {
  public static final String STATUS_ERROR = "ERROR";
  public static final String STATUS_VALID_USER = "VALID_USER";
  public static final String STATUS_VALID_TYPE = "VALID_TYPE";
  public static final String STATUS_OK = "OK";
  public static final String MSG_UNAUTHORIZED_USER = "Usuario no autorizado";

  public static final String MSG_USER_NOT_FOUND = "El usuario no existe";
  public static final String MSG_UNEXPECTED_ERROR = "Error inesperado";
  public static final String ESTADO_PENDIENTE_REVISION = "Pendiente de revisión";

  // Mensajes de log
  public static final String LOG_INIT_CREDIT_CREATION = "Iniciando proceso de creación de crédito";
  public static final String LOG_END_CREDIT_CREATION = "Proceso de creación de crédito finalizado con estado: ";
  public static final String LOG_USER_ID_MISMATCH = "El ID de usuario del token no coincide con el ID proporcionado";
  public static final String LOG_VALIDATING_USER = "Validando existencia del usuario con documento:";
  public static final String LOG_VALIDATING_CREDIT_TYPE = "Validando tipo de crédito";
  public static final String LOG_SKIP_CREDIT_TYPE_VALIDATION = "No se validará el tipo de crédito debido a un error previo";
  public static final String LOG_QUERYING_CREDIT_TYPE = "Consultando tipo de crédito con ID: ";
  public static final String LOG_INIT_CREDIT_PERSISTENCE = "Iniciando persistencia del crédito";
  public static final String LOG_SKIP_CREDIT_PERSISTENCE = "No se persistirá el crédito debido a un error previo";
  public static final String LOG_SAVING_CREDIT = "Guardando crédito para usuario ID: ";
  public static final String LOG_CREDIT_SAVED = "Crédito guardado exitosamente";
  public static final String LOG_ERROR_CREDIT_CREATION = "Error en el proceso de creación de crédito: ";
  public static final String LOG_ERROR_MESSAGE_EXTRACTED = "Mensaje de error extraído: ";
  public static final String LOG_EXTRACTING_ERROR_MESSAGE = "Extrayendo mensaje de error de excepción tipo: ";
  public static final String LOG_SKIP_USER_VALIDATION =" No se validará el usuario debido a que no existe en base de datos";
  // Separadores
  public static final String COMMA_SEPARATOR = ", ";

  public static final String LOG_FETCHING_CREDITS_LIST = "Obteniendo lista de créditos paginada Pagina: ";
  public static final String LOG_ERROR_FETCHING_CREDITS = "Error al obtener créditos";
  public static final String SIZE = " Size: ";

}
