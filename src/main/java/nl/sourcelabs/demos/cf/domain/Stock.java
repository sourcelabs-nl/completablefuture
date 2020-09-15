package nl.sourcelabs.demos.cf.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Stock {
  private int level;
  private String location;

  public static Stock unknown() {
    return Stock.builder()
        .level(0)
        .location("Stock unknown, ask customer care")
        .build();
  }

}
