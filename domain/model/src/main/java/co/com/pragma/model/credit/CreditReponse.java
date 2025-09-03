package co.com.pragma.model.credit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreditReponse {
  private String statusResponse;
  private CreditParameters creditParameters;
  private String errorMessage;
}
