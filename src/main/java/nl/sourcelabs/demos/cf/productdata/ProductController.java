package nl.sourcelabs.demos.cf.productdata;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

  private ProductDataService productDataService;
  private ProductDataService2 productDataService2;

  public ProductController(ProductDataService productDataService, ProductDataService2 productDataService2) {
    this.productDataService = productDataService;
    this.productDataService2 = productDataService2;
  }

  @GetMapping(path = "/productsv1", produces = MediaType.APPLICATION_JSON_VALUE)
  public ProductResponse getProducts() {
    return productDataService.getProducts();
  }

  @GetMapping(path = "/productsv2", produces = MediaType.APPLICATION_JSON_VALUE)
  public ProductResponse getProductsV2() {
    return productDataService2.getProducts();
  }

}
