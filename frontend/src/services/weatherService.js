import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || '/api/v1/weather';

/**
 * Weather API Service
 * Handles all API calls to the backend
 */
class WeatherService {
  
  /**
   * Get weather forecast for a city
   */
  async getWeatherForecast(city) {
    try {
      const response = await axios.get(`${API_BASE_URL}/forecast`, {
        params: { city }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching weather forecast:', error);
      throw this.handleError(error);
    }
  }
  
  /**
   * Toggle offline mode
   */
  async toggleOfflineMode(enabled) {
    try {
      const response = await axios.post(`${API_BASE_URL}/offline-mode`, null, {
        params: { enabled }
      });
      return response.data;
    } catch (error) {
      console.error('Error toggling offline mode:', error);
      throw this.handleError(error);
    }
  }
  
  /**
   * Get offline mode status
   */
  async getOfflineModeStatus() {
    try {
      const response = await axios.get(`${API_BASE_URL}/offline-mode`);
      return response.data;
    } catch (error) {
      console.error('Error getting offline mode status:', error);
      throw this.handleError(error);
    }
  }
  
  /**
   * Health check
   */
  async healthCheck() {
    try {
      const response = await axios.get(`${API_BASE_URL}/health`);
      return response.data;
    } catch (error) {
      console.error('Health check failed:', error);
      throw this.handleError(error);
    }
  }
  
  /**
   * Handle API errors
   */
  handleError(error) {
    if (error.response) {
      // Server responded with error
      return {
        message: error.response.data.message || 'An error occurred',
        status: error.response.status,
        errorCode: error.response.data.errorCode
      };
    } else if (error.request) {
      // Request made but no response
      return {
        message: 'Unable to connect to server. Please check if the backend is running.',
        status: 0
      };
    } else {
      // Something else happened
      return {
        message: error.message || 'An unexpected error occurred',
        status: -1
      };
    }
  }
}

export default new WeatherService();
