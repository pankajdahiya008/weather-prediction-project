package com.weather.service;

import com.weather.model.WeatherForecast;

/**
 * Strategy Pattern for Weather Warnings
 * Follows Open/Closed Principle - Easy to add new warning types
 */
public interface WeatherWarningStrategy {
    
    /**
     * Check if warning applies to the forecast
     */
    boolean applies(WeatherForecast forecast);
    
    /**
     * Get the warning message
     */
    String getWarningMessage();
    
    /**
     * Get priority of warning (lower number = higher priority)
     */
    int getPriority();
}
