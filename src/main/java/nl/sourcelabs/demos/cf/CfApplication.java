package nl.sourcelabs.demos.cf;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SpringBootApplication
public class CfApplication {

  public static void main(String[] args) {
    SpringApplication.run(CfApplication.class, args);
  }

  @Bean
  public Executor theExecutor(final MeterRegistry registry) {
    return ExecutorServiceMetrics
            .monitor(registry, Executors.newFixedThreadPool(10), "myExecutorPool");
  }

  @Bean
  public HttpClient myHttpClient() {
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create()
            .setDefaultRequestConfig(
                    RequestConfig.custom()
                            .setConnectTimeout(1000)
                            .setSocketTimeout(1000)
                            .build())
            .setMaxConnTotal(100)
            .setMaxConnPerRoute(50);
    return httpClientBuilder.build();
  }

}
