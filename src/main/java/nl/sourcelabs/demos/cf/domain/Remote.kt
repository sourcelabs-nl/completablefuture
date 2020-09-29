package nl.sourcelabs.demos.cf.domain

data class RemoteProductResponse(val products: List<Product> = emptyList())

data class RemoteProductPrice(val price: String? = null, val currency: String? = null)

data class RemoteProductWarehouse(val warehouseId: String? = null, val name: String? = null, val locationId: String? = null)

data class RemoteProductStock(val stockCount: String? = null)

data class RemoteProductReview(val descr: String?, val stars: Int?)