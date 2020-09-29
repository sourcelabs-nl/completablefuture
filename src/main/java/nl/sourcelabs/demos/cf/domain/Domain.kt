package nl.sourcelabs.demos.cf.domain

data class Price(val amount: String? = null, val currency: String? = null)

data class Product(
    val id: String? = null,
    val title: String? = null,
    val price: Price? = null,
    val stock: Stock? = null,
    val reviews: List<Review>? = null
)

data class Review(val stars: Int = 0, val comment: String? = null)

data class Stock(val level: Int = 0, val location: String? = "Stock unknown, ask customer care")