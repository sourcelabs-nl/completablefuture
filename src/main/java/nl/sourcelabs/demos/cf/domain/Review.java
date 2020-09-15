package nl.sourcelabs.demos.cf.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {
  private int stars;
  private String comment;
}
