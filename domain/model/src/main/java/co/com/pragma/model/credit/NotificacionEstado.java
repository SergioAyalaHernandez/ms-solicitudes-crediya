package co.com.pragma.model.credit;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class NotificacionEstado {
  public String estadoSolicitud;
  public String correoElectronico;
}
