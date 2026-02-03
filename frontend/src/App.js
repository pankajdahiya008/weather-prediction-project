import React, { useState, useEffect } from 'react';
import WeatherCard from './components/WeatherCard';
import weatherService from './services/weatherService';
import './styles/App.css';

/**
 * Main App Component
 * Weather Prediction Application
 */
function App() {
  const [city, setCity] = useState('London');
  const [weatherData, setWeatherData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [offlineMode, setOfflineMode] = useState(false);
  const [searchCity, setSearchCity] = useState('London');
  
  // Load initial weather data
  useEffect(() => {
    fetchWeatherData(city);
    checkOfflineMode();
  }, []);
  
  const fetchWeatherData = async (cityName) => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await weatherService.getWeatherForecast(cityName);
      setWeatherData(data);
      setCity(cityName);
    } catch (err) {
      setError(err.message || 'Failed to fetch weather data');
      setWeatherData(null);
    } finally {
      setLoading(false);
    }
  };
  
  const checkOfflineMode = async () => {
    try {
      const status = await weatherService.getOfflineModeStatus();
      setOfflineMode(status);
    } catch (err) {
      console.error('Failed to check offline mode:', err);
    }
  };
  
  const handleSearch = (e) => {
    e.preventDefault();
    if (searchCity.trim()) {
      fetchWeatherData(searchCity.trim());
    }
  };
  
  const handleToggleOfflineMode = async () => {
    try {
      await weatherService.toggleOfflineMode(!offlineMode);
      setOfflineMode(!offlineMode);
      // Refresh weather data
      fetchWeatherData(city);
    } catch (err) {
      setError('Failed to toggle offline mode');
    }
  };
  
  return (
    <div className="App">
      <header className="App-header">
        <h1>🌤️ Weather Prediction</h1>
        <p className="subtitle">3-Day Weather Forecast</p>
      </header>
      
      <div className="controls-section">
        <form onSubmit={handleSearch} className="search-form">
          <input
            type="text"
            placeholder="Enter city name..."
            value={searchCity}
            onChange={(e) => setSearchCity(e.target.value)}
            className="search-input"
          />
          <button type="submit" className="search-button">
            Search
          </button>
        </form>
        
        <div className="offline-toggle">
          <label className="toggle-label">
            <input
              type="checkbox"
              checked={offlineMode}
              onChange={handleToggleOfflineMode}
            />
            <span className="toggle-text">Offline Mode</span>
          </label>
        </div>
      </div>
      
      {loading && (
        <div className="loading">
          <div className="spinner"></div>
          <p>Loading weather data...</p>
        </div>
      )}
      
      {error && (
        <div className="error-message">
          <p>⚠️ {error}</p>
        </div>
      )}
      
      {weatherData && !loading && (
        <div className="weather-container">
          <div className="weather-header">
            <h2>{weatherData.city}, {weatherData.country}</h2>
            {weatherData.dataSource && (
              <span className={`data-source ${weatherData.dataSource}`}>
                {weatherData.dataSource === 'offline' ? '📴 Offline Data' : '🌐 Live Data'}
              </span>
            )}
          </div>
          
          {weatherData.message && (
            <div className="info-message">
              ℹ️ {weatherData.message}
            </div>
          )}
          
          <div className="forecasts-grid">
            {weatherData.forecasts && weatherData.forecasts.map((forecast, index) => (
              <WeatherCard key={index} forecast={forecast} />
            ))}
          </div>
        </div>
      )}
      
      <footer className="App-footer">
        <p>Weather data provided by OpenWeatherMap API</p>
        <p className="tech-stack">
          Built with React + Spring Boot | Microservices Architecture
        </p>
      </footer>
    </div>
  );
}

export default App;
