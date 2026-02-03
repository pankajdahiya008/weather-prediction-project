import React from 'react';
import '../styles/WeatherCard.css';

/**
 * WeatherCard Component
 * Displays weather forecast for a single day
 */
const WeatherCard = ({ forecast }) => {
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const options = { weekday: 'short', month: 'short', day: 'numeric' };
    return date.toLocaleDateString('en-US', options);
  };
  
  const getWeatherIcon = (weather) => {
    const weatherLower = weather.toLowerCase();
    if (weatherLower.includes('rain')) return '🌧️';
    if (weatherLower.includes('thunder')) return '⛈️';
    if (weatherLower.includes('cloud')) return '☁️';
    if (weatherLower.includes('clear') || weatherLower.includes('sunny')) return '☀️';
    return '🌤️';
  };
  
  const getWarningIcon = (warning) => {
    if (warning.includes('umbrella')) return '☂️';
    if (warning.includes('sunscreen')) return '🧴';
    if (warning.includes('windy')) return '💨';
    if (warning.includes('Storm')) return '⚠️';
    return '⚠️';
  };
  
  return (
    <div className="weather-card">
      <div className="weather-card-header">
        <h3>{formatDate(forecast.date)}</h3>
        <span className="weather-icon">{getWeatherIcon(forecast.weather)}</span>
      </div>
      
      <div className="weather-card-body">
        <div className="temperature">
          <div className="temp-item">
            <span className="temp-label">High</span>
            <span className="temp-value">{Math.round(forecast.tempMax)}°C</span>
          </div>
          <div className="temp-item">
            <span className="temp-label">Low</span>
            <span className="temp-value">{Math.round(forecast.tempMin)}°C</span>
          </div>
        </div>
        
        <div className="weather-details">
          <p><strong>Weather:</strong> {forecast.weather}</p>
          <p><strong>Wind:</strong> {forecast.windSpeed.toFixed(1)} mph</p>
        </div>
        
        {forecast.warnings && forecast.warnings.length > 0 && (
          <div className="warnings">
            <h4>⚠️ Warnings:</h4>
            {forecast.warnings.map((warning, index) => (
              <div key={index} className="warning-item">
                <span className="warning-icon">{getWarningIcon(warning)}</span>
                <span className="warning-text">{warning}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default WeatherCard;
