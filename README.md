1. Start mockserver
    1. Go to mockserver folder and type docker-compose up -d
    2. Go to http://localhost:1080/mockserver/dashboard to see loaded expectations
    3. No mockserver restart needed when updating cfdata.json
2. Start spring boot app CfApplication: ./mvnw spring-boot:run
3. Go to http://localhost:8080/productsv1
4. Look at log to check duration of the call


Scenario:

Return list of products including price, stock level, reviews with 5 different services
1. http://localhost:1080/products list products including productId
2. http://localhost:1080/prices?productId={} price for product (May not produce error or no data, if error, skip product)
3. http://localhost:1080/warehouses?productId={} returns locationId for stock query (if error return stock unknown)
4. http://localhost:1080/locations?locationId={}&productId={} returns stock for product (if error return stock unknown)
5. http://localhost:1080/reviews?productId={} list of reviews, on error empty list

ProductDataService 
Blocking calls for all

Steps:
1. Open ProductService2 and analyze each call
2. Convert calls into CompletableFuture calls
3. Update code to run futures in parallel where applicable
4. Go to http://localhost:8080/productsv2
5. Compare duration in logs with duration of productsv1 call
6. Make it even faster!! 
