package com.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * External API Response Models
 * Follows Open/Closed Principle - open for extension
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherResponse {
    
    private String cod;
    private Integer cnt;
    private List<ForecastItem> list;
    private CityInfo city;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ForecastItem {
        private Long dt;
        private MainInfo main;
        private List<Weather> weather;
        private Wind wind;
        private String dt_txt;
        
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MainInfo {
            private Double temp;
            private Double temp_min;
            private Double temp_max;
        }
        
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Weather {
            private String main;
            private String description;
        }
        
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Wind {
            private Double speed;
        }
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CityInfo {
        private String name;
        private String country;
    }
}
