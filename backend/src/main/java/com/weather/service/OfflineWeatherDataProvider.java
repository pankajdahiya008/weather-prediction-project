package com.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.exception.WeatherServiceException;
import com.weather.model.WeatherForecast;
import com.weather.model.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Offline Weather Data Provider - Uses static JSON data
 * Implements Strategy Pattern
 * Provides fallback when API is unavailable
 */
@Slf4j
@Service
public class OfflineWeatherDataProvider implements WeatherDataProvider {
    
    @Value("${weather.offline.data-file}")
    private Resource offlineDataFile;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, List<WeatherForecast>> offlineData = new HashMap<>();
    
    @PostConstruct
    public void loadOfflineData() {
        try {
            log.info("Loading offline weather data from: {}", offlineDataFile.getFilename());
            JsonNode rootNode = objectMapper.readTree(offlineDataFile.getInputStream());
            
            rootNode.fields().forEachRemaining(entry -> {
                String city = entry.getKey();
                JsonNode forecastsNode = entry.getValue().get("forecasts");
                
                List<WeatherForecast> forecasts = new ArrayList<>();
                forecastsNode.forEach(node -> {
                    WeatherForecast forecast = WeatherForecast.builder()
                            .date(LocalDate.parse(node.get("date").asText()))
                            .tempMax(node.get("tempMax").asDouble())
                            .tempMin(node.get("tempMin").asDouble())
                            .weather(node.get("weather").asText())
                            .windSpeed(node.get("windSpeed").asDouble())
                            .hasRain(node.get("hasRain").asBoolean())
                            .hasThunderstorm(node.get("hasThunderstorm").asBoolean())
                            .build();
                    forecasts.add(forecast);
                });
                
                offlineData.put(city.toLowerCase(), forecasts);
            });
            
            log.info("Loaded offline data for {} cities", offlineData.size());
            
        } catch (Exception e) {
            log.error("Failed to load offline weather data", e);
        }
    }
    
    @Override
    public WeatherResponse getWeatherForecast(String city) {
        log.info("Fetching offline weather data for city: {}", city);
        
        String cityKey = city.toLowerCase().replaceAll("\\s+", "");
        List<WeatherForecast> forecasts = offlineData.get(cityKey);
        
        if (forecasts == null || forecasts.isEmpty()) {
            throw new WeatherServiceException(
                    "No offline data available for city: " + city,
                    "NO_OFFLINE_DATA"
            );
        }
        
        return WeatherResponse.builder()
                .city(city)
                .country("Offline Mode")
                .forecasts(new ArrayList<>(forecasts))
                .dataSource("offline")
                .message("Using cached data - API unavailable")
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    @Override
    public boolean isAvailable() {
        return !offlineData.isEmpty();
    }
    
    @Override
    public String getProviderName() {
        return "offline";
    }
}
