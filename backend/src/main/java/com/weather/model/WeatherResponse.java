package com.weather.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

/**
 * Response Model for Weather API
 * Implements HATEOAS principle by extending RepresentationModel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherResponse extends RepresentationModel<WeatherResponse> {
    
    private String city;
    private String country;
    private List<WeatherForecast> forecasts;
    private String dataSource; // "online" or "offline"
    private String message;
    private Long timestamp;
}
