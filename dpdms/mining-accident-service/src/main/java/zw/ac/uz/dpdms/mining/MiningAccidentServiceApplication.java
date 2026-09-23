package zw.ac.uz.dpdms.mining;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * IMPORTANT when copying this pattern: @ComponentScan must list BOTH
 * this service's own base package AND zw.ac.uz.dpdms.common, or Spring
 * won't find HazardScopeGuard / RequestContextResolver (they live in
 * the common module, a different top-level package than this service's
 * own classes, so Spring Boot's default single-package scan misses
 * them).
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"zw.ac.uz.dpdms.mining", "zw.ac.uz.dpdms.common"})
public class MiningAccidentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiningAccidentServiceApplication.class, args);
    }
}
