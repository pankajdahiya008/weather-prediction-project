package com.weather.controller;

import com.weather.model.WeatherResponse;
import com.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Weather Prediction REST Controller
 * 
 * Demonstrates:
 * - HATEOAS: Hypermedia as the Engine of Application State
 * - RESTful API Design
 * - OpenAPI/Swagger Documentation
 * - Error Handling
 * 
 * Error Codes:
 * - WEATHER_ERROR: General weather service error
 * - API_ERROR: External API call failed
 * - NO_DATA: No weather data received
 * - FETCH_ERROR: Unable to fetch weather data
 * - NO_OFFLINE_DATA: No offline data available for city
 * - INVALID_INPUT: Invalid input parameter
 * - INTERNAL_ERROR: Unexpected server error
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Tag(name = "Weather Forecast", description = "Weather prediction API for 3-day forecasts")
public class WeatherController {
    
    private final WeatherService weatherService;
    
    /**
     * Get 3-day weather forecast for a city
     */
    @Operation(
        summary = "Get weather forecast",
        description = "Retrieves 3-day weather forecast for the specified city with warnings"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved weather forecast",
            content = @Content(schema = @Schema(implementation = WeatherResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid city parameter"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error or API unavailable"
        )
    })
    @GetMapping("/forecast")
    public ResponseEntity<WeatherResponse> getWeatherForecast(
            @Parameter(description = "City name", required = true, example = "London")
            @RequestParam String city) {
        
        log.info("Received request for weather forecast: city={}", city);
        
        WeatherResponse response = weatherService.getWeatherForecast(city);
        
        // Add HATEOAS links
        addHateoasLinks(response, city);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Toggle offline mode
     */
    @Operation(
        summary = "Toggle offline mode",
        description = "Enable or disable offline mode for the service"
    )
    @PostMapping("/offline-mode")
    public ResponseEntity<String> toggleOfflineMode(
            @Parameter(description = "Enable offline mode", required = true)
            @RequestParam boolean enabled) {
        
        log.info("Toggling offline mode: {}", enabled);
        weatherService.setOfflineMode(enabled);
        
        return ResponseEntity.ok(
            String.format("Offline mode %s", enabled ? "enabled" : "disabled")
        );
    }
    
    /**
     * Get offline mode status
     */
    @Operation(
        summary = "Get offline mode status",
        description = "Check if offline mode is currently enabled"
    )
    @GetMapping("/offline-mode")
    public ResponseEntity<Boolean> getOfflineModeStatus() {
        boolean status = weatherService.isOfflineModeEnabled();
        log.info("Offline mode status: {}", status);
        return ResponseEntity.ok(status);
    }
    
    /**
     * Health check endpoint
     */
    @Operation(
        summary = "Health check",
        description = "Check if the service is running"
    )
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Weather Service is running");
    }
    
    /**
     * Add HATEOAS links to response
     * Demonstrates HATEOAS principle
     */
    private void addHateoasLinks(WeatherResponse response, String city) {
        try {
            // Self link
            Link selfLink = linkTo(methodOn(WeatherController.class)
                    .getWeatherForecast(city))
                    .withSelfRel();
            response.add(selfLink);
            
            // Offline mode toggle link
            Link offlineModeLink = linkTo(methodOn(WeatherController.class)
                    .toggleOfflineMode(false))
                    .withRel("toggle-offline-mode");
            response.add(offlineModeLink);
            
            // Health check link
            Link healthLink = linkTo(methodOn(WeatherController.class)
                    .healthCheck())
                    .withRel("health");
            response.add(healthLink);
            
        } catch (Exception e) {
            log.warn("Failed to add HATEOAS links", e);
        }
    }
}
