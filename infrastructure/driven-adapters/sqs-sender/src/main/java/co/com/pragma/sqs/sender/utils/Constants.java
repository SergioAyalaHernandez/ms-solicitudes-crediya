package co.com.pragma.sqs.sender.utils;

public class Constants {

  public static final String LOG_SENDING_MESSAGE = "Intentando enviar mensaje a la cola SQS de notificacion : {}";
  public static final String LOG_SENDING_MESSAGE_REPORT = "Intentando enviar mensaje a la cola SQS de reportes: {}";
  public static final String LOG_MESSAGE_SENT = "Mensaje enviado correctamente de notificaciones. MessageId: {}";
  public static final String LOG_MESSAGE_SENT_REPORT = "Mensaje enviado correctamente de reporte. MessageId: {}";
  public static final String LOG_ERROR_SENDING_MESSAGE = "Error al enviar mensaje a la cola SQS";
}
