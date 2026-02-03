package com.weather.service;

import com.weather.model.WeatherForecast;
import com.weather.model.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Main Weather Service
 * Demonstrates:
 * - Facade Pattern: Provides simple interface to complex subsystem
 * - Strategy Pattern: Uses different data providers
 * - Template Method Pattern: Common flow with variable steps
 * 
 * SOLID Principles:
 * - Single Responsibility: Coordinates weather data fetching and processing
 * - Open/Closed: Open for extension through new strategies
 * - Liskov Substitution: Data providers are interchangeable
 * - Interface Segregation: Clean interface contracts
 * - Dependency Inversion: Depends on abstractions (interfaces)
 */
@Slf4j
@Service
public class WeatherService {
    
    private final OnlineWeatherDataProvider onlineProvider;
    private final OfflineWeatherDataProvider offlineProvider;
    private final List<WeatherWarningStrategy> warningStrategies;
    
    @Value("${weather.offline.enabled:false}")
    private boolean offlineModeEnabled;
    
    public WeatherService(
            OnlineWeatherDataProvider onlineProvider,
            OfflineWeatherDataProvider offlineProvider,
            List<WeatherWarningStrategy> warningStrategies) {
        this.onlineProvider = onlineProvider;
        this.offlineProvider = offlineProvider;
        this.warningStrategies = warningStrategies;
    }
    
    /**
     * Get weather forecast with caching
     * Cache eviction can be configured via application properties
     * Cache key includes offline mode to prevent stale data when mode changes
     */
    @Cacheable(value = "weather-forecasts", key = "#city + '_' + #root.target.offlineModeEnabled")
    public WeatherResponse getWeatherForecast(String city) {
        log.info("Fetching weather forecast for city: {}, offlineMode: {}", 
                city, offlineModeEnabled);
        
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty");
        }
        
        WeatherResponse response = fetchWeatherData(city);
        applyWeatherWarnings(response);
        
        return response;
    }
    
    /**
     * Fetch weather data using appropriate provider
     * Demonstrates Strategy Pattern
     */
    private WeatherResponse fetchWeatherData(String city) {
        try {
            if (offlineModeEnabled) {
                log.info("Using offline data provider for city: {}", city);
                WeatherResponse response = offlineProvider.getWeatherForecast(city);
                log.info("Successfully fetched offline data for city: {}, dataSource: {}", city, response.getDataSource());
                return response;
            }
            
            // Try online first, fallback to offline
            log.info("Using online data provider for city: {}", city);
            if (onlineProvider.isAvailable()) {
                WeatherResponse response = onlineProvider.getWeatherForecast(city);
                log.info("Successfully fetched online data for city: {}, dataSource: {}", city, response.getDataSource());
                return response;
            } else {
                log.warn("Online provider unavailable, falling back to offline data");
                WeatherResponse response = offlineProvider.getWeatherForecast(city);
                log.info("Successfully fetched fallback offline data for city: {}, dataSource: {}", city, response.getDataSource());
                return response;
            }
            
        } catch (Exception e) {
            log.error("Error fetching from primary provider, trying fallback", e);
            
            if (!offlineModeEnabled && offlineProvider.isAvailable()) {
                log.info("Falling back to offline data due to error");
                WeatherResponse response = offlineProvider.getWeatherForecast(city);
                log.info("Successfully fetched error fallback offline data for city: {}, dataSource: {}", city, response.getDataSource());
                return response;
            }
            
            throw e;
        }
    }
    
    /**
     * Apply warning strategies to weather forecasts
     * Demonstrates Strategy Pattern for extensible business rules
     */
    private void applyWeatherWarnings(WeatherResponse response) {
        if (response.getForecasts() == null) {
            return;
        }
        
        response.getForecasts().forEach(forecast -> {
            // Sort strategies by priority and apply
            warningStrategies.stream()
                    .filter(strategy -> strategy.applies(forecast))
                    .sorted(Comparator.comparingInt(WeatherWarningStrategy::getPriority))
                    .forEach(strategy -> {
                        forecast.addWarning(strategy.getWarningMessage());
                        log.debug("Applied warning: {} for date: {}", 
                                strategy.getWarningMessage(), forecast.getDate());
                    });
        });
    }
    
    /**
     * Toggle offline mode at runtime
     * Clears cache when mode changes to ensure fresh data
     */
    @CacheEvict(value = "weather-forecasts", allEntries = true)
    public void setOfflineMode(boolean enabled) {
        log.info("Switching offline mode to: {}, clearing cache", enabled);
        this.offlineModeEnabled = enabled;
    }
    
    /**
     * Check current offline mode status
     */
    public boolean isOfflineModeEnabled() {
        return offlineModeEnabled;
    }
}
