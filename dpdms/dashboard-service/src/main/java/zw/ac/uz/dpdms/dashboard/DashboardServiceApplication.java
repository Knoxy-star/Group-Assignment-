package zw.ac.uz.dpdms.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Provincial dashboard service.
 * Shows APPROVED incidents from every hazard service on one page.
 * It has no database: the page's JavaScript calls each hazard
 * service's REST API through the gateway.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class DashboardServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DashboardServiceApplication.class, args);
    }
}