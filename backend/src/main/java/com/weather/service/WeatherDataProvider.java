package com.weather.service;

import com.weather.model.WeatherResponse;

/**
 * Strategy Pattern for Weather Data Providers
 * Allows switching between online and offline data sources
 * Follows Dependency Inversion Principle
 */
public interface WeatherDataProvider {
    
    /**
     * Get weather forecast for a city
     */
    WeatherResponse getWeatherForecast(String city);
    
    /**
     * Check if provider is available
     */
    boolean isAvailable();
    
    /**
     * Get provider name
     */
    String getProviderName();
}
