package zw.ac.uz.dpdms.drought;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ComponentScan lists BOTH this service's package and the common
 * module's package, so HazardScopeGuard / RequestContextResolver are found.
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"zw.ac.uz.dpdms.drought", "zw.ac.uz.dpdms.common"})
public class DroughtServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DroughtServiceApplication.class, args);
    }
}
