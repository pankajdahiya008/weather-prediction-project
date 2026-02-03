package com.weather;

import com.weather.controller.WeatherController;
import com.weather.model.WeatherForecast;
import com.weather.model.WeatherResponse;
import com.weather.service.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for WeatherController
 * Tests REST API endpoints
 */
@WebMvcTest(WeatherController.class)
@DisplayName("Weather Controller Integration Tests")
class WeatherControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private WeatherService weatherService;
    
    @Test
    @DisplayName("GET /api/v1/weather/forecast should return weather data")
    void testGetWeatherForecast_Success() throws Exception {
        // Given
        String city = "London";
        WeatherResponse mockResponse = createMockResponse(city);
        when(weatherService.getWeatherForecast(city)).thenReturn(mockResponse);
        
        // When & Then
        mockMvc.perform(get("/api/v1/weather/forecast")
                        .param("city", city)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value(city))
                .andExpect(jsonPath("$.forecasts").isArray())
                .andExpect(jsonPath("$.forecasts.length()").value(3));
    }
    
    @Test
    @DisplayName("GET /api/v1/weather/forecast without city should return 400")
    void testGetWeatherForecast_MissingCity_BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/weather/forecast")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("POST /api/v1/weather/offline-mode should toggle offline mode")
    void testToggleOfflineMode_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/weather/offline-mode")
                        .param("enabled", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Offline mode enabled"));
    }
    
    @Test
    @DisplayName("GET /api/v1/weather/offline-mode should return status")
    void testGetOfflineModeStatus_Success() throws Exception {
        // Given
        when(weatherService.isOfflineModeEnabled()).thenReturn(false);
        
        // When & Then
        mockMvc.perform(get("/api/v1/weather/offline-mode")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
    
    @Test
    @DisplayName("GET /api/v1/weather/health should return health status")
    void testHealthCheck_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/weather/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Weather Service is running"));
    }
    
    private WeatherResponse createMockResponse(String city) {
        WeatherForecast forecast1 = WeatherForecast.builder()
                .date(LocalDate.now())
                .tempMax(25.0)
                .tempMin(15.0)
                .weather("Clear")
                .windSpeed(5.0)
                .hasRain(false)
                .hasThunderstorm(false)
                .build();
        
        WeatherForecast forecast2 = WeatherForecast.builder()
                .date(LocalDate.now().plusDays(1))
                .tempMax(26.0)
                .tempMin(16.0)
                .weather("Cloudy")
                .windSpeed(6.0)
                .hasRain(false)
                .hasThunderstorm(false)
                .build();
        
        WeatherForecast forecast3 = WeatherForecast.builder()
                .date(LocalDate.now().plusDays(2))
                .tempMax(24.0)
                .tempMin(14.0)
                .weather("Sunny")
                .windSpeed(7.0)
                .hasRain(false)
                .hasThunderstorm(false)
                .build();
        
        return WeatherResponse.builder()
                .city(city)
                .country("UK")
                .forecasts(Arrays.asList(forecast1, forecast2, forecast3))
                .dataSource("online")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
