package nl.sourcelabs.demos.cf.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Product {
  private String id;
  private String title;
  private Price price;
  private Stock stock;
  private List<Review> reviews;
}
