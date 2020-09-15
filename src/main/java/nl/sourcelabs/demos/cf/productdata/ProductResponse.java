package nl.sourcelabs.demos.cf.productdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import nl.sourcelabs.demos.cf.domain.Product;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductResponse {
    private List<Product> products;
}


