package zw.ac.uz.dpdms.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @ComponentScan lists both this service's package and
 * zw.ac.uz.dpdms.common, so RequestContextResolver is found.
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"zw.ac.uz.dpdms.report", "zw.ac.uz.dpdms.common"})
public class ReportServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReportServiceApplication.class, args);
    }
}
