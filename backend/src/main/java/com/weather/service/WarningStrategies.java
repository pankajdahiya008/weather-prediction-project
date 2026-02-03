package com.weather.service;

import com.weather.model.WeatherForecast;
import org.springframework.stereotype.Component;

/**
 * Concrete implementations of Weather Warning Strategies
 * Demonstrates Strategy Pattern for extensible business logic
 */

@Component
class ThunderstormWarningStrategy implements WeatherWarningStrategy {
    
    @Override
    public boolean applies(WeatherForecast forecast) {
        return forecast.needsThunderstormWarning();
    }
    
    @Override
    public String getWarningMessage() {
        return "Don't step out! A Storm is brewing!";
    }
    
    @Override
    public int getPriority() {
        return 1; // Highest priority
    }
}

@Component
class HighWindWarningStrategy implements WeatherWarningStrategy {
    
    @Override
    public boolean applies(WeatherForecast forecast) {
        return forecast.needsWindWarning();
    }
    
    @Override
    public String getWarningMessage() {
        return "It's too windy, watch out!";
    }
    
    @Override
    public int getPriority() {
        return 2;
    }
}

@Component
class HighTemperatureWarningStrategy implements WeatherWarningStrategy {
    
    @Override
    public boolean applies(WeatherForecast forecast) {
        return forecast.needsSunscreenWarning();
    }
    
    @Override
    public String getWarningMessage() {
        return "Use sunscreen lotion";
    }
    
    @Override
    public int getPriority() {
        return 3;
    }
}

@Component
class RainWarningStrategy implements WeatherWarningStrategy {
    
    @Override
    public boolean applies(WeatherForecast forecast) {
        return forecast.needsUmbrellaWarning();
    }
    
    @Override
    public String getWarningMessage() {
        return "Carry umbrella";
    }
    
    @Override
    public int getPriority() {
        return 4;
    }
}
