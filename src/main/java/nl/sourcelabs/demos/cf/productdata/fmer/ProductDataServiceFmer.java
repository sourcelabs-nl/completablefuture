package nl.sourcelabs.demos.cf.productdata.fmer;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.sourcelabs.demos.cf.domain.Product;
import nl.sourcelabs.demos.cf.productdata.ProductResponse;
import org.apache.http.client.HttpClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductDataServiceFmer {

  private RestTemplate restTemplate;
  private final Executor myExecutor;// = new ForkJoinPool(180);

  public ProductDataServiceFmer(RestTemplateBuilder builder, Executor theExecutor, HttpClient myHttpClient) {
    restTemplate = builder
            .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(myHttpClient))
            .rootUri("http://localhost:1080")
            .build();
    myExecutor = theExecutor;

    //myExcutor = new ForkJoinPool(10); //ForkJoinPool.commonPool();

//            new ThreadPoolTaskExecutor();
//    myExcutor.setCorePoolSize(10);
//    myExcutor.setMaxPoolSize(10);
//    myExcutor.setQueueCapacity(500);
//    myExcutor.setThreadNamePrefix("MyCustomPool-");
//    myExcutor.initialize();
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
    log.info("Give me cores {}", Runtime.getRuntime().availableProcessors());
    //  Number of threads = Number of Available Cores * (1 + Wait time / Service time)
    // 12 * (1 + 70/5)

    StopWatch sp = new StopWatch();
    sp.start();
    try {
      log.info("GETTING PRODUCTS");
      List<ProductDataLoader> loaders = getProductList().getProducts().stream()
          .map(product -> new ProductDataLoader(restTemplate, product, myExecutor))
          .collect(Collectors.toList());

      List<CompletableFuture> futures = loaders.stream()
          .map(pl -> pl.getFutures())
          .flatMap(List::stream)
          .collect(Collectors.toList());

      try {
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get();
      } catch (InterruptedException | ExecutionException e) {
        log.error("It is not ok... but it is...");
      }
      log.info("DONE GETTING PRODUCTS");
      return new ProductResponse(
          loaders.stream()
              .map(ProductDataLoader::complete)
              .filter(Objects::nonNull)
              .collect(Collectors.toList())
      );
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
