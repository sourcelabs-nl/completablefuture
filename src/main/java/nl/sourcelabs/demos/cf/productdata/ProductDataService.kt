package nl.sourcelabs.demos.cf.productdata

import io.micrometer.core.annotation.Timed
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.StopWatch
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Service
class ProductDataService(private val restTemplate: RestTemplate) { // TODO replace with WebClient

    private val LOG = LoggerFactory.getLogger(ProductDataService::class.java)

    /***
     * 1. /products list products
     * 2. /prices?productId={} price for product (May not produce error or no data, if error, skip product)
     * 3. /warehouses?productId={} returns locationId for stock query (if error return default)
     * 4. /locations?locationId={}&productId={} returns stock for product (can produce error)
     * 5. /reviews?productId={} list of reviews
     */
    @Timed
    suspend fun getProducts(): ProductResponse = withContext(Dispatchers.IO) {
        val map = getProductList()
                .map {
                    ProductEnriched(
                            it,
                            async { getProductPrice(it.id) },
                            async { getProductReviews(it.id) },
                            async { getProductStock(it.id) }
                    )
                }

        ProductResponse(map.mapNotNull { it.awaitAll() })
    }

    private fun getProductList(): List<RemoteProduct> = try {
        LOG.info("getProductList")
        restTemplate.getForObject("/products", RemoteProductResponse::class.java)?.products ?: listOf()
    } catch (e: Exception) {
        listOf()
    }

    private fun getProductPrice(productId: String): RemoteProductPrices? = try {
        LOG.info("getProductPrice")
        restTemplate.getForObject("/prices?productId=$productId")
    } catch (e: Exception) {
        null
    }

    private fun getProductReviews(productId: String): List<RemoteProductReview>? = try {
        LOG.info("getProductReviews")
        restTemplate.getForObject<Array<RemoteProductReview>>("/reviews?productId=$productId").toList()
    } catch (e: Exception) {
        null
    }

    private fun getProductStock(productId: String): Stock {
        val rpw: RemoteProductWarehouse? = try {
            LOG.info("RemoteProductWarehouse")
            restTemplate.getForObject("/warehouses?productId=$productId")
        } catch (e: Exception) {
            null
        }

        val rps: RemoteProductStock? = rpw?.let {
            try {
                LOG.info("RemoteProductStock")
                restTemplate.getForObject("/locations?productId=" + productId + "&locationId=" + rpw.locationId)
            } catch (e: Exception) {
                null
            }
        }

        return Stock(location = rpw?.name ?: "unknown", level = rps?.stockCount?.toInt() ?: 0)
    }

    data class RemoteProduct(val id: String, val title: String)

    data class RemoteProductResponse(val products: List<RemoteProduct>)

    data class RemoteProductPrices(val price: String, val currency: String)

    data class RemoteProductReview(val descr: String, val stars: String)

    data class RemoteProductWarehouse(val warehouseId: String, val name: String, val locationId: String)

    data class RemoteProductStock(val stockCount: String)

    data class ProductEnriched(val product: RemoteProduct, val price: Deferred<RemoteProductPrices?>, val reviews: Deferred<List<RemoteProductReview>?>, val stock: Deferred<Stock>) {
        private val LOG = LoggerFactory.getLogger(ProductDataService::class.java)

        suspend fun awaitAll(): Product? {
            val sw = StopWatch().apply { start() }

            val priceResult = price.await()
            val reviewsResult: List<RemoteProductReview>? = reviews.await()
            val stockResult: Stock = stock.await()

            sw.stop()
            LOG.info("Awaited results for product: ${product.id} - ${sw.totalTimeMillis}")

            if (priceResult == null) return null

            return Product(id = product.id,
                    title = product.title,
                    price = Price(amount = priceResult.price, currency = priceResult.currency),
                    reviews = reviewsResult?.map { Review(stars = it.stars.toInt(), comment = it.descr) } ?: listOf(),
                    stock = stockResult
            )
        }
    }
}