package nl.sourcelabs.demos.cf.productdata

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class ProductController(private val productDataService: ProductDataService) {

    @GetMapping(path = ["/productsv2"])
    suspend fun getProductsV2() = productDataService.getProducts()
}