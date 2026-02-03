package com.weather.exception;

/**
 * Custom Exception for Weather Service
 * Follows Single Responsibility Principle
 */
public class WeatherServiceException extends RuntimeException {
    
    private final String errorCode;
    
    public WeatherServiceException(String message) {
        super(message);
        this.errorCode = "WEATHER_ERROR";
    }
    
    public WeatherServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public WeatherServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "WEATHER_ERROR";
    }
    
    public WeatherServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
