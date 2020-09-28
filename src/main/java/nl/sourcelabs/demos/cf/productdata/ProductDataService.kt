package nl.sourcelabs.demos.cf.productdata;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.sourcelabs.demos.cf.domain.Product;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class ProductDataService2 {

  private RestTemplate restTemplate;

  public ProductDataService2(RestTemplateBuilder builder) {
    restTemplate = builder
        .rootUri("http://localhost:1080")
        .build();
  }

  /***
   * 1. /products list products
   * 2. /prices?productId={} price for product (May not produce error or no data, if error, skip product)
   * 3. /warehouses?productId={} returns locationId for stock query (if error return default)
   * 4. /locations?locationId={}&productId={} returns stock for product (can produce error)
   * 5. /reviews?productId={} list of reviews
   * @return
   */
  public ProductResponse getProducts() {
    StopWatch sp = new StopWatch();
    sp.start();
    try {
      return null;
    }
    finally {
      sp.stop();
      log.info("Duration:" + sp.getTotalTimeMillis() + "ms");
    }
  }

  // First call to get all products
  private RemoteProductResponse getProductList() {
    return restTemplate.getForObject("/products", RemoteProductResponse.class);
  }

  @Data
  @NoArgsConstructor
  private static class RemoteProductResponse {

    private List<Product> products;
  }

}
