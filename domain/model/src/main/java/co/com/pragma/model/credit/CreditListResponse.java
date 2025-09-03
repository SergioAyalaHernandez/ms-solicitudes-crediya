package co.com.pragma.model.credit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreditListResponse {
  private String statusResponse;
  private List<CreditDetailDTO> creditDetailDTO;
  private Long totalCredits;
  private String errorMessage;
}
