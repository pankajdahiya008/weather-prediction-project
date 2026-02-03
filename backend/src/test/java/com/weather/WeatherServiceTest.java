package com.weather;

import com.weather.model.WeatherForecast;
import com.weather.model.WeatherResponse;
import com.weather.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for WeatherService
 * Demonstrates TDD and BDD principles
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Weather Service Tests")
class WeatherServiceTest {
    
    @Mock
    private OnlineWeatherDataProvider onlineProvider;
    
    @Mock
    private OfflineWeatherDataProvider offlineProvider;
    
    private WeatherService weatherService;
    private List<WeatherWarningStrategy> warningStrategies;
    
    @BeforeEach
    void setUp() {
        // Mock warning strategies since they are package-private
        WeatherWarningStrategy thunderstormStrategy = mock(WeatherWarningStrategy.class);
        WeatherWarningStrategy windStrategy = mock(WeatherWarningStrategy.class);
        WeatherWarningStrategy tempStrategy = mock(WeatherWarningStrategy.class);
        WeatherWarningStrategy rainStrategy = mock(WeatherWarningStrategy.class);

        warningStrategies = Arrays.asList(
                thunderstormStrategy,
                windStrategy,
                tempStrategy,
                rainStrategy
        );
        
        weatherService = new WeatherService(
                onlineProvider,
                offlineProvider,
                warningStrategies
        );
    }
    
    @Test
    @DisplayName("Should fetch weather forecast successfully from online provider")
    void testGetWeatherForecast_Online_Success() {
        // Given
        String city = "London";
        WeatherResponse mockResponse = createMockWeatherResponse(city);
        when(onlineProvider.isAvailable()).thenReturn(true);
        when(onlineProvider.getWeatherForecast(city)).thenReturn(mockResponse);
        
        // When
        WeatherResponse response = weatherService.getWeatherForecast(city);
        
        // Then
        assertNotNull(response);
        assertEquals(city, response.getCity());
        assertEquals(3, response.getForecasts().size());
        verify(onlineProvider, times(1)).getWeatherForecast(city);
    }
    
    @Test
    @DisplayName("Should fallback to offline provider when online fails")
    void testGetWeatherForecast_Fallback_ToOffline() {
        // Given
        String city = "London";
        WeatherResponse mockResponse = createMockWeatherResponse(city);
        when(onlineProvider.isAvailable()).thenReturn(true);
        when(onlineProvider.getWeatherForecast(city))
                .thenThrow(new RuntimeException("API Error"));
        when(offlineProvider.isAvailable()).thenReturn(true);
        when(offlineProvider.getWeatherForecast(city)).thenReturn(mockResponse);
        
        // When
        WeatherResponse response = weatherService.getWeatherForecast(city);
        
        // Then
        assertNotNull(response);
        verify(offlineProvider, times(1)).getWeatherForecast(city);
    }
    
    @Test
    @DisplayName("Should apply high temperature warning when temp > 40")
    void testWeatherWarnings_HighTemperature() {
        // Given
        String city = "Dubai";
        WeatherResponse mockResponse = createMockWeatherResponseWithHighTemp(city);
        when(onlineProvider.isAvailable()).thenReturn(true);
        when(onlineProvider.getWeatherForecast(city)).thenReturn(mockResponse);
        
        // When
        WeatherResponse response = weatherService.getWeatherForecast(city);
        
        // Then
        assertTrue(response.getForecasts().get(0).getWarnings()
                .contains("Use sunscreen lotion"));
    }
    
    @Test
    @DisplayName("Should apply umbrella warning when rain is predicted")
    void testWeatherWarnings_Rain() {
        // Given
        String city = "Seattle";
        WeatherResponse mockResponse = createMockWeatherResponseWithRain(city);
        when(onlineProvider.isAvailable()).thenReturn(true);
        when(onlineProvider.getWeatherForecast(city)).thenReturn(mockResponse);
        
        // When
        WeatherResponse response = weatherService.getWeatherForecast(city);
        
        // Then
        assertTrue(response.getForecasts().get(0).getWarnings()
                .contains("Carry umbrella"));
    }
    
    @Test
    @DisplayName("Should apply wind warning when wind > 10mph")
    void testWeatherWarnings_HighWind() {
        // Given
        String city = "Chicago";
        WeatherResponse mockResponse = createMockWeatherResponseWithHighWind(city);
        when(onlineProvider.isAvailable()).thenReturn(true);
        when(onlineProvider.getWeatherForecast(city)).thenReturn(mockResponse);
        
        // When
        WeatherResponse response = weatherService.getWeatherForecast(city);
        
        // Then
        assertTrue(response.getForecasts().get(0).getWarnings()
                .contains("It's too windy, watch out!"));
    }
    
    @Test
    @DisplayName("Should apply thunderstorm warning")
    void testWeatherWarnings_Thunderstorm() {
        // Given
        String city = "Miami";
        WeatherResponse mockResponse = createMockWeatherResponseWithThunderstorm(city);
        when(onlineProvider.isAvailable()).thenReturn(true);
        when(onlineProvider.getWeatherForecast(city)).thenReturn(mockResponse);
        
        // When
        WeatherResponse response = weatherService.getWeatherForecast(city);
        
        // Then
        assertTrue(response.getForecasts().get(0).getWarnings()
                .contains("Don't step out! A Storm is brewing!"));
    }
    
    @Test
    @DisplayName("Should throw exception for empty city name")
    void testGetWeatherForecast_EmptyCity_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
                () -> weatherService.getWeatherForecast(""));
    }
    
    @Test
    @DisplayName("Should toggle offline mode")
    void testToggleOfflineMode() {
        // When
        weatherService.setOfflineMode(true);
        
        // Then
        assertTrue(weatherService.isOfflineModeEnabled());
        
        // When
        weatherService.setOfflineMode(false);
        
        // Then
        assertFalse(weatherService.isOfflineModeEnabled());
    }
    
    // Helper methods to create mock data
    private WeatherResponse createMockWeatherResponse(String city) {
        return WeatherResponse.builder()
                .city(city)
                .forecasts(Arrays.asList(
                        createMockForecast(LocalDate.now(), 25.0, 15.0, false, false, 5.0),
                        createMockForecast(LocalDate.now().plusDays(1), 26.0, 16.0, false, false, 6.0),
                        createMockForecast(LocalDate.now().plusDays(2), 24.0, 14.0, false, false, 7.0)
                ))
                .build();
    }
    
    private WeatherResponse createMockWeatherResponseWithHighTemp(String city) {
        return WeatherResponse.builder()
                .city(city)
                .forecasts(Arrays.asList(
                        createMockForecast(LocalDate.now(), 42.0, 30.0, false, false, 5.0)
                ))
                .build();
    }
    
    private WeatherResponse createMockWeatherResponseWithRain(String city) {
        return WeatherResponse.builder()
                .city(city)
                .forecasts(Arrays.asList(
                        createMockForecast(LocalDate.now(), 20.0, 15.0, true, false, 8.0)
                ))
                .build();
    }
    
    private WeatherResponse createMockWeatherResponseWithHighWind(String city) {
        return WeatherResponse.builder()
                .city(city)
                .forecasts(Arrays.asList(
                        createMockForecast(LocalDate.now(), 18.0, 12.0, false, false, 15.0)
                ))
                .build();
    }
    
    private WeatherResponse createMockWeatherResponseWithThunderstorm(String city) {
        return WeatherResponse.builder()
                .city(city)
                .forecasts(Arrays.asList(
                        createMockForecast(LocalDate.now(), 22.0, 18.0, true, true, 12.0)
                ))
                .build();
    }
    
    private WeatherForecast createMockForecast(LocalDate date, double maxTemp, 
                                                double minTemp, boolean hasRain, 
                                                boolean hasThunderstorm, double windSpeed) {
        return WeatherForecast.builder()
                .date(date)
                .tempMax(maxTemp)
                .tempMin(minTemp)
                .hasRain(hasRain)
                .hasThunderstorm(hasThunderstorm)
                .windSpeed(windSpeed)
                .weather("Clear")
                .build();
    }
}
