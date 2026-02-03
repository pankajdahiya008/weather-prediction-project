package com.weather.service;

import com.weather.exception.WeatherServiceException;
import com.weather.model.OpenWeatherResponse;
import com.weather.model.WeatherForecast;
import com.weather.model.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Online Weather Data Provider - Fetches from OpenWeather API
 * Implements Strategy Pattern
 * Demonstrates Dependency Injection
 */
@Slf4j
@Service
public class OnlineWeatherDataProvider implements WeatherDataProvider {
    
    private final WebClient webClient;
    private final String apiKey;
    
    @Value("${weather.api.timeout:5000}")
    private int timeout;
    
    public OnlineWeatherDataProvider(
            @Value("${weather.api.base-url}") String baseUrl,
            @Value("${weather.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
    }
    
    @Override
    public WeatherResponse getWeatherForecast(String city) {
        try {
            log.info("Fetching online weather data for city: {}", city);
            
            OpenWeatherResponse apiResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("q", city)
                            .queryParam("appid", apiKey)
                            .queryParam("cnt", 24) // 3 days worth of data
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .bodyToMono(OpenWeatherResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .onErrorResume(WebClientResponseException.class, e -> {
                        log.error("API call failed: {}", e.getMessage());
                        return Mono.error(new WeatherServiceException(
                                "Failed to fetch weather data: " + e.getStatusCode(),
                                "API_ERROR"
                        ));
                    })
                    .block();
            
            if (apiResponse == null || apiResponse.getList() == null) {
                throw new WeatherServiceException("No weather data received", "NO_DATA");
            }
            
            return buildWeatherResponse(apiResponse, city);
            
        } catch (Exception e) {
            log.error("Error fetching weather data", e);
            throw new WeatherServiceException(
                    "Unable to fetch weather data for city: " + city,
                    "FETCH_ERROR",
                    e
            );
        }
    }
    
    private WeatherResponse buildWeatherResponse(OpenWeatherResponse apiResponse, String city) {
        // Group forecasts by date and get daily min/max
        Map<LocalDate, List<OpenWeatherResponse.ForecastItem>> forecastsByDate = 
                apiResponse.getList().stream()
                        .collect(Collectors.groupingBy(item -> 
                                Instant.ofEpochSecond(item.getDt())
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                        ));
        
        // Get next 3 days
        List<WeatherForecast> forecasts = forecastsByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(3)
                .map(entry -> buildDailyForecast(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        
        String country = apiResponse.getCity() != null ? 
                apiResponse.getCity().getCountry() : "Unknown";
        
        return WeatherResponse.builder()
                .city(city)
                .country(country)
                .forecasts(forecasts)
                .dataSource("online")
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    private WeatherForecast buildDailyForecast(LocalDate date, 
                                                List<OpenWeatherResponse.ForecastItem> items) {
        double maxTemp = items.stream()
                .mapToDouble(item -> item.getMain().getTemp_max())
                .max()
                .orElse(0.0);
        
        double minTemp = items.stream()
                .mapToDouble(item -> item.getMain().getTemp_min())
                .min()
                .orElse(0.0);
        
        double avgWindSpeed = items.stream()
                .mapToDouble(item -> item.getWind().getSpeed())
                .average()
                .orElse(0.0);
        
        // Check for rain and thunderstorm
        boolean hasRain = items.stream()
                .anyMatch(item -> item.getWeather().stream()
                        .anyMatch(w -> w.getMain().toLowerCase().contains("rain")));
        
        boolean hasThunderstorm = items.stream()
                .anyMatch(item -> item.getWeather().stream()
                        .anyMatch(w -> w.getMain().toLowerCase().contains("thunderstorm")));
        
        String weatherDesc = items.isEmpty() ? "Unknown" : 
                items.get(0).getWeather().isEmpty() ? "Unknown" :
                items.get(0).getWeather().get(0).getMain();
        
        return WeatherForecast.builder()
                .date(date)
                .tempMax(maxTemp)
                .tempMin(minTemp)
                .weather(weatherDesc)
                .windSpeed(avgWindSpeed)
                .hasRain(hasRain)
                .hasThunderstorm(hasThunderstorm)
                .build();
    }
    
    @Override
    public boolean isAvailable() {
        return true; // Can be enhanced to check API health
    }
    
    @Override
    public String getProviderName() {
        return "online";
    }
}
