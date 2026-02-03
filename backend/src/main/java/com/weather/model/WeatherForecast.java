package com.weather.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain Model for Weather Forecast
 * Follows SOLID Principles - Single Responsibility
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherForecast {
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    private Double tempMax;
    private Double tempMin;
    private String weather;
    private Double windSpeed;
    private Boolean hasRain;
    private Boolean hasThunderstorm;
    
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    
    /**
     * Business logic to add weather warnings based on conditions
     * Demonstrates Strategy Pattern for extensible warning conditions
     */
    public void addWarning(String warning) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        if (!warnings.contains(warning)) {
            warnings.add(warning);
        }
    }
    
    /**
     * Check if high temperature warning is needed
     */
    public boolean needsSunscreenWarning() {
        return tempMax != null && tempMax > 40.0;
    }
    
    /**
     * Check if rain warning is needed
     */
    public boolean needsUmbrellaWarning() {
        return hasRain != null && hasRain;
    }
    
    /**
     * Check if wind warning is needed
     */
    public boolean needsWindWarning() {
        return windSpeed != null && windSpeed > 10.0;
    }
    
    /**
     * Check if thunderstorm warning is needed
     */
    public boolean needsThunderstormWarning() {
        return hasThunderstorm != null && hasThunderstorm;
    }
}
