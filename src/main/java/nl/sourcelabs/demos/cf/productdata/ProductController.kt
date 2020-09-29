package nl.sourcelabs.demos.cf.productdata

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/"], produces = [MediaType.APPLICATION_JSON_VALUE])
class ProductController(private val productDataService: ProductDataService) {
   
    @GetMapping(path = ["/productsv2"], produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getProducts(): ProductResponse {
        return productDataService.getProducts()
    }
}