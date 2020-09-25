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

Starting tips
1. Changing call to ComputableFuture
```
private CompletableFuture<RemoteProductPrices> getProductPrice(String productId) {
   return CompletableFuture.supplyAsync(() -> {
       return restTemplate.getForObject("/prices?productId=" + productId, RemoteProductPrices.class);
   });
}
```

2. Handling exceptions
    .exceptionally
    
3. Use result of ComputalbleFuture or create something new
    .thenApply "map" or .thenCompose "flatMap"   

4. User custom thread pool for execution, tweak values for more performance
```
    ThreadPoolTaskExecutor myExcutor = new ThreadPoolTaskExecutor();
    myExcutor.setCorePoolSize(2);
    myExcutor.setMaxPoolSize(2);
    myExcutor.setQueueCapacity(500);
    myExcutor.setThreadNamePrefix("MyCustomPool-");
    myExcutor.initialize();
```
more info:

https://howtodoinjava.com/java/multi-threading/java-thread-pool-executor-example/

https://dzone.com/articles/be-aware-of-forkjoinpoolcommonpool

5. Monitor threads by looking at spring boot metrics actuator.  Add management.endpoints.web.exposure.include: '*' to application.yml and create metric for your pool as in example below

```
  @Bean
  public ExecutorService myExecutor(final MeterRegistry registry) {
    return ExecutorServiceMetrics
        .monitor(registry, Executors.newFixedThreadPool(10), "myExecutorPool");
  }
```

6. Configure HttpClient for your rest templates
```
  @Bean
  public HttpClient myHttpClient(MyProps properties) {
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
        .setDefaultRequestConfig(
            RequestConfig.custom()
                .setConnectTimeout(Math.toIntExact(properties.getConnectionTimeout().toMillis()))
                .setSocketTimeout(Math.toIntExact(properties.getReadTimeout().toMillis()))
                .build())
        .setMaxConnTotal(100)
        .setMaxConnPerRoute(50);
    return httpClientBuilder.build();
  }

  @Bean
  RestTemplate myTemplate(RestTemplateBuilder builder, MyProps properties) {
    return builder
        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(myHttpClient(properties)))
        .rootUri(properties.getRootUri())
        .build();
  }
```

7. Wait for many to complete

```
CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
```
