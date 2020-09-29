package nl.sourcelabs.demos.cf.productdata

import nl.sourcelabs.demos.cf.domain.Product

data class ProductResponse(val products: List<Product> = emptyList())