package nl.sourcelabs.demos.cf.productdata;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.sourcelabs.demos.cf.domain.Price;
import nl.sourcelabs.demos.cf.domain.Product;
import nl.sourcelabs.demos.cf.domain.Review;
import nl.sourcelabs.demos.cf.domain.Stock;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductDataService {

    private RestTemplate restTemplate;

    public ProductDataService(RestTemplateBuilder builder) {
        restTemplate = builder
                .rootUri("http://localhost:1080")
                .build();
    }

    /***
     * 1. /products list products
     * 2. /prices?productId={} price for product (May not produce error or no data)
     * 3. /warehouses?productId={} returns locationId for stock query (can product default or error)
     * 4. /locations?locationId={}&productId={} returns stock for product (can produce default on error)
     * 5. /reviews?productId={} list of reviews
     * @return
     */
    public ProductResponse getProducts() {
        StopWatch sp = new StopWatch();
        sp.start();
        try {
            return new ProductResponse(
                    getProductList().getProducts().stream()
                            .map(this::addData)
                            .collect(Collectors.toList())
            );
        } finally {
            sp.stop();
            log.info("Duration:" + sp.getTotalTimeMillis() + "ms");
        }

    }

    private Product addData(Product initialProduct) {
        setProductReviews(initialProduct);
        setProductPrice(initialProduct);
        setProductStock(initialProduct);
        return initialProduct;
    }

    private void setProductReviews(Product initialProduct) {
        try {
            initialProduct.setReviews(
                    getProductReviews(initialProduct.getId()).stream()
                            .map(rpr -> Review.builder()
                                    .stars(Integer.valueOf(rpr.stars))
                                    .comment(rpr.descr)
                                    .build())
                            .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("oops, review is broken for product {}", initialProduct.getId());
        }
    }

    private void setProductPrice(Product initialProduct) {
        try {
            RemoteProductPrices rpp = getProductPrice(initialProduct.getId());
            initialProduct.setPrice(Price.builder()
                    .amount(rpp.price)
                    .currency(rpp.getCurrency())
                    .build());
        } catch (Exception e) {
            log.error("oops, price is broken for product {}", initialProduct.getId());
        }
    }

    private void setProductStock(Product initialProduct) {
        try {
            initialProduct.setStock(getProductStock(initialProduct.getId()));
        } catch (Exception e) {
            log.error("oops, stock is broken for product {}", initialProduct.getId());
        }
    }

    // First call to get all products
    private RemoteProductResponse getProductList() {
        return restTemplate.getForObject("/products", RemoteProductResponse.class);
    }

    // Get price for a product
    private RemoteProductPrices getProductPrice(String productId) {
        return restTemplate.getForObject("/prices?productId=" + productId, RemoteProductPrices.class);
    }

    // Get reviews for a product
    private List<RemoteProductReview> getProductReviews(String productId) {
        return Arrays.asList(restTemplate.getForObject("/reviews?productId=" + productId, RemoteProductReview[].class));
    }

    // Get stock for a product
    private Stock getProductStock(String productId) {
        RemoteProductWarehouse rpw = restTemplate
                .getForObject("/warehouses?productId=" + productId, RemoteProductWarehouse.class);
        RemoteProductStock rps = restTemplate
                .getForObject("/locations?productId=" + productId + "&locationId=" + rpw.getLocationId(),
                        RemoteProductStock.class);
        return Stock.builder()
                .location(rpw.getName())
                .level(Integer.valueOf(rps.getStockCount()))
                .build();
    }

    @Data
    @NoArgsConstructor
    private static class RemoteProductResponse {

        private List<Product> products;
    }

    @Data
    @NoArgsConstructor
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
