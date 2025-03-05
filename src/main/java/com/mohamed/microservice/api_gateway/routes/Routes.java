package com.mohamed.microservice.api_gateway.routes;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions.circuitBreaker;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration
public class Routes {

    @Bean
    public RouterFunction<ServerResponse> productServiceRoute() {
        return route("product_service")
                .route(RequestPredicates.path("api/v1/products"), HandlerFunctions.http("http://localhost:8080"))
                .filter(circuitBreaker("productServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> orderRoute() {
        return route("order_service")
                .route(RequestPredicates.path("api/v1/products"), HandlerFunctions.http("http://localhost:8081"))
                .filter(circuitBreaker("orderServiceCircuitBreaker", URI.create("forward://fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> inventoryRoute() {
        return route("inventory_service")
                .route(RequestPredicates.path("api/v1/inventory"), HandlerFunctions.http("http://localhost:8083"))
                .filter(circuitBreaker("inventoryServiceCircuitBreaker", URI.create("forward://fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return route("fallbackRoute")
                .GET("/fallbackRoute", request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Service Unavailable, please try again later"))
                .build();
    }
}
