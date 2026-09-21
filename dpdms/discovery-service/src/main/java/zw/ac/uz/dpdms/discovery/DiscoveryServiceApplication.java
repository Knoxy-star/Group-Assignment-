package zw.ac.uz.dpdms.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * DPDMS Discovery Service.
 *
 * This is the Eureka registry. Every other microservice (gateway,
 * auth-service, the five hazard services, report-service, alert-service,
 * dashboard-service) registers itself here on startup, and the gateway
 * uses this registry to route requests without hardcoding host:port pairs.
 *
 * Run this FIRST, before any other service. Dashboard is at
 * http://localhost:8761 once it's up - useful for confirming which
 * services are currently registered and healthy.
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServiceApplication.class, args);
    }
}
