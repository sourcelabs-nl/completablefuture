package nl.sourcelabs.demos.cf.domain;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Price {
  private String amount;
  private String currency;
}
