package co.com.pragma.api.utils;

public class Constants {
  public static final String API_BASE_PATH = "/api/v1";
  public static final String LOGIN_PATH = API_BASE_PATH + "/login";
  public static final String USERS_PATH = API_BASE_PATH + "/usuarios";
  public static final String USERS_PATH_WILDCARD = USERS_PATH + "/**";
  public static final String REQUESTS_PATH_WILDCARD = API_BASE_PATH + "/solicitudes/**";
  public static final String SOLICITUD_PATH = API_BASE_PATH + "/solicitud";
  public static final String SOLICITUDES_PATH = API_BASE_PATH + "/solicitudes";
  public static final String ERROR_CREDIT_NOT_FOUND = "Crédito con ID %d no encontrado";
  public static final String ID = "id";

  public static final String WEBJARS_PATH = "/webjars/**";
  public static final String SWAGGER_UI_PATH = "/swagger-ui/**";
  public static final String SWAGGER_RESOURCES_PATH = "/swagger-resources/**";
  public static final String[] API_DOCS_PATHS = {"/v3/api-docs/**", "/v2/api-docs/**"};

  // Roles
  public static final String ROLE_ADMIN = "ADMIN";
  public static final String ROLE_ADVISOR = "ASESOR";
  public static final String ROLE_USER = "USER";
  public static final String ROLE_CLIENT = "CLIENTE";
  public static final String ROLE_PREFIX = "ROLE_";

  // Auth
  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";

  // Headers
  public static final String HEADER_AUTHORIZATION = "Authorization";

  // Query params default
  public static final String DEFAULT_PAGE = "0";
  public static final String DEFAULT_SIZE = "10";

  // Mensajes de error
  public static final String ERROR_CREATE_CREDIT = "Ocurrió un error inesperado: ";
  public static final String ERROR_GET_CREDITS = "Error al obtener la lista de créditos: ";
  public static final String ERROR_UPDATE_CREDIT = "Error al actualizar el crédito: ";

  // Tamaños
  public static final String SIZE = "size";
  public static final String PAGE = "page";

  // Errores
  public static final String TOKEN_ERROR = "Token inválido";


}
