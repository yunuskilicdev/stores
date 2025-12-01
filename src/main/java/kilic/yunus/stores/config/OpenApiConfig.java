package kilic.yunus.stores.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Jumbo Store Locator API")
                                .version("1.0.0")
                                .description(
                                        "REST API for finding the nearest Jumbo stores based on geographical location. "
                                                + "Uses the Haversine formula for accurate distance calculations.")
                                .contact(new Contact().name("Yunus Kilic").email("yunuskilicdev@gmail.com"))
                                .license(
                                        new License().name("MIT License").url("https://opensource.org/licenses/MIT")));
    }
}
