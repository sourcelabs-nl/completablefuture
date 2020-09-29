package nl.sourcelabs.demos.cf.productdata

import kotlinx.coroutines.*
import nl.sourcelabs.demos.cf.domain.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Service
class ProductDataService(
    private val client: Client
) {

    suspend fun getProducts() = withContext(Dispatchers.IO) {
        ProductResponse(
            products = client.loadProducts()
                .map { async { enrich(it) } }
                .awaitAll()
                .filter { it.price != null }
        )
    }

    private suspend fun enrich(product: Product) = coroutineScope {
        val price = async {
            client.loadPrices(product)?.let { Price(amount = it.price, currency = it.currency) }
        }

        val stock = async {
            client.loadWarehouses(product)?.let {
                val remoteStock = client.loadStock(product, it.locationId!!)
                Stock(level = remoteStock?.stockCount?.toInt() ?: 0, location = it.name)
            }
        }

        val reviews = async {
            client.loadReviews(product).map {
                Review(stars = it.stars ?: 0, comment = it.descr ?: "n/a")
            }
        }

        product.copy(
            price = price.await(),
            stock = stock.await(),
            reviews = reviews.await()
        )
    }
}

@Service
class Client(private val restTemplate: RestTemplate) {

    suspend fun loadProducts(): List<Product> = try {
        restTemplate.getForObject<RemoteProductResponse>("/products").products
    } catch (exception: Exception) {
        emptyList()
    }

    suspend fun loadPrices(product: Product): RemoteProductPrice? = try {
        restTemplate.getForObject("/prices?productId=${product.id}")
    } catch (exception: Exception) {
        null
    }

    suspend fun loadWarehouses(product: Product): RemoteProductWarehouse? = try {
        restTemplate.getForObject("/warehouses?productId=${product.id}")
    } catch (exception: Exception) {
        null
    }

    suspend fun loadStock(product: Product, locationId: String): RemoteProductStock? = try {
        restTemplate.getForObject("/locations?productId=${product.id}&locationId=${locationId}")
    } catch (exception: Exception) {
        null
    }

    suspend fun loadReviews(product: Product): List<RemoteProductReview> = try {
        restTemplate.getForObject<Array<RemoteProductReview>>("/reviews?productId=${product.id}").toList()
    } catch (exception: Exception) {
        emptyList()
    }
}