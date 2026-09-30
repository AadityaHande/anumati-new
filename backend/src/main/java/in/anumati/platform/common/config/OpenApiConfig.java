package in.anumati.platform.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI anumatiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Anumati API")
                        .version("v1")
                        .description("Regulatory operations APIs for industrial approvals, evidence, compliance and government workflow management.\n\n" +
                                "Regulatory applicability is deterministic and source-backed. Government decisions remain with authorised departments."))
                .components(new Components().addSecuritySchemes("sessionCookie",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("ANUMATI_SESSION")
                                .description("Authenticated Anumati session cookie.")))
                .addSecurityItem(new SecurityRequirement().addList("sessionCookie"));
    }
}
