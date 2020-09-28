package nl.sourcelabs.demos.cf.productdata

data class Price (val amount: String, val currency: String)

data class Product(val id: String, val title: String, val price: Price, val stock: Stock, val reviews: List<Review>)

data class Review (val stars: Int = 0, val comment: String)

data class Stock(val level: Int = 0, val location: String)