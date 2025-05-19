package com.ssafy.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//Swagger-UI 확인
//http://localhost/swagger-ui.html

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI aptOpenAPI() {
        return new OpenAPI().info(new Info().title("APT 프로젝트").description("<h3>아파트 아파트!</h3>").version("1.0"));
    }
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder().group("all").pathsToMatch("/**").build();
    }

    @Bean
    public GroupedOpenApi aptApi() {
        return GroupedOpenApi.builder().group("apt").pathsToMatch("/apt/**").build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder().group("user").pathsToMatch("/user/**").build();
    }

    @Bean
    public GroupedOpenApi reviewApi() {
        return GroupedOpenApi.builder().group("review").pathsToMatch("/review/**").build();
    }

}
