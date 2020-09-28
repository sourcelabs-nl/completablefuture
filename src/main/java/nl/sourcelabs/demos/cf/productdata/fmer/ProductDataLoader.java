package nl.sourcelabs.demos.cf.productdata.fmer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.sourcelabs.demos.cf.domain.Price;
import nl.sourcelabs.demos.cf.domain.Product;
import nl.sourcelabs.demos.cf.domain.Review;
import nl.sourcelabs.demos.cf.domain.Stock;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
class ProductDataLoader {

  private RestTemplate restTemplate;
  private Product product;
  private CompletableFuture<RemoteProductPrices> priceFuture;
  private CompletableFuture<Stock> stockFuture;
  private CompletableFuture<List<RemoteProductReview>> reviewFuture;

  //private ExecutorService myPool = ForkJoinPool.commonPool();
  //private ExecutorService myPool = Executors.newFixedThreadPool(10);
  private final Executor myPool;

  public ProductDataLoader(RestTemplate restTemplate, Product product, Executor executor) {
    this.restTemplate = restTemplate;
    this.product = product;
    this.myPool = executor;
    priceFuture = getProductPrice(product.getId());
    stockFuture = getProductStock(product.getId());
    reviewFuture = getProductReviews(product.getId());
  }

  public List<CompletableFuture> getFutures() {
    return Arrays.asList(priceFuture, stockFuture, reviewFuture);
  }

  public Product complete() {
    log.info("START loading data for product {}", product.getId());
    try {
      setProductReviews();
      setProductPrice();
      setProductStock();
      log.info("END loading data product {}", product.getId());
    } catch (ExecutionException | InterruptedException e) {
      log.error("Error loading product {} details, skipping product", product.getId());
      // if fetching row produce error skip row in result
      return null;
    }
    return product;
  }

  // Get price for a product
  private CompletableFuture<RemoteProductPrices> getProductPrice(String productId) {
    return CompletableFuture.supplyAsync(() -> {
      log.info("Thread: {}", Thread.currentThread());
      return restTemplate.getForObject("/prices?productId=" + productId, RemoteProductPrices.class);
    }, myPool);
  }

  // Get reviews for a product
  private CompletableFuture<List<RemoteProductReview>> getProductReviews(String productId) {
    return CompletableFuture.supplyAsync(() -> {
      log.info("Thread: {}", Thread.currentThread());
      return Arrays.asList(restTemplate.getForObject("/reviews?productId=" + productId, RemoteProductReview[].class));
    }, myPool)
        .exceptionally(throwable -> {
          log.error("error loading reviews product {}", productId);
          return Collections.emptyList();
        });
  }

  // Get stock for a product
  private CompletableFuture<Stock> getProductStock(String productId) {
    return CompletableFuture.supplyAsync(() -> {
      log.info("Thread: {}", Thread.currentThread());
      return restTemplate.getForObject("/warehouses?productId=" + productId, RemoteProductWarehouse.class);
    }, myPool)
        .exceptionally(throwable -> {
          log.error("error loading warehouse for product {}", productId);
          return null;
        })
        .thenApply(rpw -> {
          if (rpw == null) {
            return Stock.unknown();
          }
          RemoteProductStock rps = restTemplate
              .getForObject("/locations?productId=" + productId + "&locationId=" + rpw.getLocationId(),
                  RemoteProductStock.class);
          return Stock.builder()
              .location(rpw.getName())
              .level(Integer.valueOf(rps.getStockCount()))
              .build();
        })
        .exceptionally(throwable -> {
          log.error("error loading location for productId {}", productId);
          return Stock.unknown();
        });
  }

  private void setProductReviews() throws InterruptedException, ExecutionException {
    product.setReviews(
        reviewFuture.get().stream()
            .map(rpr -> Review.builder()
                .stars(Integer.valueOf(rpr.stars))
                .comment(rpr.descr)
                .build())
            .collect(Collectors.toList()));
  }

  private void setProductPrice() throws InterruptedException, ExecutionException {
    RemoteProductPrices rpp = priceFuture.get();
    product.setPrice(Price.builder()
        .amount(rpp.price)
        .currency(rpp.getCurrency())
        .build());
  }

  private void setProductStock() throws ExecutionException, InterruptedException {
    product.setStock(stockFuture.get());
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  private static class RemoteProductPrices {

    private String price;
    private String currency;
  }

  @Data
  @NoArgsConstructor
  private static class RemoteProductReview {

    private String descr;
    private String stars;
  }

  @Data
  @NoArgsConstructor
  private static class RemoteProductWarehouse {

    private String warehouseId;
    private String name;
    private String locationId;
  }

  @Data
  @NoArgsConstructor
  private static class RemoteProductStock {

    private String stockCount;
  }

}
