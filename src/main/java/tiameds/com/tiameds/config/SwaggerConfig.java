package tiameds.com.tiameds.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPIConfig() {
        // Create server definitions
        Server devServer = new Server()
                .url("http://localhost:8080")
                .description("Development server");

        Server testServer = new Server()
                .url("http://localhost:8081")
                .description("Testing server");

        Server prodServer = new Server()
                .url("https://tiameds.com")
                .description("Production server");



        return new OpenAPI()
                .servers(Arrays.asList(devServer, testServer, prodServer)) // Add servers to OpenAPI
                .tags(
                        Arrays.asList(
                                new Tag().name("Admin Controller").description("Operations pertaining to admin management"),
                                new Tag().name("User Controller").description("Operations pertaining to user management")

                        )
                )
                .info(new Info()
                        .title("Tiameds API")
                        .version("1.0")
                        .description("Tiameds API Documentation") // Custom description
                        .contact(new Contact()
                                .name("Tiameds Support")
                                .url("https://tiameds.com/support") // Update with your support URL
                                .email("support@tiameds.com")) // Update with your support email
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html"))); // License info
    }
}


