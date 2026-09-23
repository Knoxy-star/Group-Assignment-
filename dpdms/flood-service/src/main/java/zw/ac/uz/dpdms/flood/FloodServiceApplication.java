package zw.ac.uz.dpdms.flood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ComponentScan must list BOTH this service's own base package AND
 * zw.ac.uz.dpdms.common, or Spring won't find HazardScopeGuard /
 * RequestContextResolver (they live in the common module).
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"zw.ac.uz.dpdms.flood", "zw.ac.uz.dpdms.common"})
public class FloodServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FloodServiceApplication.class, args);
    }
}
