# Setup Guide - Weather Prediction Project

Complete step-by-step guide to set up and run the project.

---

## 📋 Prerequisites

### Required Software
- ✅ **Java 17** or higher
  ```bash
  java -version  # Should show 17.x or higher
  ```

- ✅ **Maven 3.6+**
  ```bash
  mvn -version
  ```

- ✅ **Node.js 18+** and **npm**
  ```bash
  node -version  # Should show 18.x or higher
  npm -version
  ```

- ✅ **Docker & Docker Compose** (optional, for containerized deployment)
  ```bash
  docker --version
  docker-compose --version
  ```

### Recommended IDE
- **IntelliJ IDEA** (Ultimate or Community)
- **VS Code** with extensions:
  - Java Extension Pack
  - Spring Boot Extension Pack
  - ESLint
  - Prettier

---

## 🚀 Quick Start (5 minutes)

### Option 1: Docker (Recommended)

```bash
# 1. Navigate to project directory
cd weather-prediction-project

# 2. Build and start all services
docker-compose up --build

# 3. Access application
# Backend: http://localhost:8080
# Frontend: http://localhost:3000
# Swagger: http://localhost:8080/swagger-ui.html
```

### Option 2: Local Development

**Terminal 1 - Backend:**
```bash
cd weather-prediction-project/backend
mvn clean install
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd weather-prediction-project/frontend
npm install
npm start
```

---

## 📝 Detailed Setup Instructions

### Step 1: Clone/Extract Project

```bash
# If from zip file
unzip weather-prediction-project.zip
cd weather-prediction-project

# If from Git
git clone <repository-url>
cd weather-prediction-project
```

### Step 2: Verify Project Structure

```
weather-prediction-project/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/weather/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── services/
│   │   ├── styles/
│   │   ├── App.js
│   │   └── index.js
│   ├── package.json
│   └── Dockerfile
├── docker-compose.yml
├── Jenkinsfile
└── README.md
```

### Step 3: Configure Backend

#### A. Application Properties

File: `backend/src/main/resources/application.properties`

```properties
# Server Configuration
server.port=8080

# API Configuration (OpenWeather)
weather.api.base-url=https://api.openweathermap.org/data/2.5
weather.api.key=d2929e9483efc82c82c32ee7e02d563e

# Offline Mode
weather.offline.enabled=false

# CORS (adjust for your frontend URL)
weather.cors.allowed-origins=http://localhost:3000,http://localhost:8080
```

**Note**: The API key provided works with the OpenWeatherMap forecast endpoint.

#### B. Build Backend

```bash
cd backend

# Clean install (downloads dependencies, compiles, runs tests)
mvn clean install

# Skip tests if needed (not recommended)
mvn clean install -DskipTests
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 45.234 s
```

#### C. Run Backend

```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Using JAR
java -jar target/weather-prediction-service-1.0.0.jar

# Option 3: With specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Verify Backend is Running**:
```bash
curl http://localhost:8080/api/v1/weather/health
# Expected: "Weather Service is running"

# Test API
curl "http://localhost:8080/api/v1/weather/forecast?city=London"
```

### Step 4: Configure Frontend

#### A. Environment Variables (Optional)

Create `.env` file in `frontend/` directory:
```env
REACT_APP_API_URL=http://localhost:8080/api/v1/weather
```

#### B. Install Dependencies

```bash
cd frontend

# Install all dependencies
npm install

# Or use npm ci for clean install
npm ci
```

Expected output:
```
added 1234 packages in 30s
```

#### C. Run Frontend

```bash
# Development mode (with hot reload)
npm start

# Build for production
npm run build

# Test
npm test
```

**Verify Frontend is Running**:
- Browser should automatically open to `http://localhost:3000`
- You should see the Weather Prediction UI

### Step 5: Test the Application

#### Manual Testing

1. **Search for a City**:
   - Enter "London" in search box
   - Click "Search"
   - Should see 3-day forecast

2. **Toggle Offline Mode**:
   - Check "Offline Mode" checkbox
   - Search for "London" again
   - Should see cached data with orange badge

3. **Test Different Cities**:
   - Try: New York, Tokyo, Paris, Mumbai
   - Observe different weather conditions and warnings

4. **Check API Directly**:
   ```bash
   # Get forecast
   curl "http://localhost:8080/api/v1/weather/forecast?city=London"
   
   # Toggle offline mode
   curl -X POST "http://localhost:8080/api/v1/weather/offline-mode?enabled=true"
   
   # Check offline mode status
   curl "http://localhost:8080/api/v1/weather/offline-mode"
   ```

#### Automated Testing

**Backend Tests**:
```bash
cd backend

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=WeatherServiceTest

# Run with coverage
mvn clean test jacoco:report
# Report: target/site/jacoco/index.html
```

**Frontend Tests**:
```bash
cd frontend

# Run tests
npm test

# Run with coverage
npm test -- --coverage --watchAll=false
```

---

## 🐳 Docker Deployment

### Build Images Manually

```bash
# Backend
cd backend
docker build -t weather-backend:1.0 .

# Frontend  
cd frontend
docker build -t weather-frontend:1.0 .
```

### Run with Docker Compose

```bash
# Start all services
docker-compose up

# Start in detached mode
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Rebuild and start
docker-compose up --build
```

### Verify Docker Deployment

```bash
# Check running containers
docker ps

# Expected output:
# CONTAINER ID   IMAGE                  STATUS         PORTS
# abc123...      weather-frontend       Up 2 minutes   0.0.0.0:3000->80/tcp
# def456...      weather-backend        Up 2 minutes   0.0.0.0:8080->8080/tcp

# Check logs
docker logs weather-backend
docker logs weather-frontend

# Test health
curl http://localhost:8080/actuator/health
```

---

## 🔧 Troubleshooting

### Backend Issues

**Problem: Port 8080 already in use**
```bash
# Find process using port 8080
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Kill the process
kill -9 <PID>  # macOS/Linux
taskkill /PID <PID> /F  # Windows

# Or change port in application.properties
server.port=8081
```

**Problem: Maven build fails**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Retry with debug
mvn clean install -X

# Check Java version
java -version  # Must be 17+
```

**Problem: API key not working**
```bash
# Verify API key
curl "https://api.openweathermap.org/data/2.5/forecast?q=London&appid=d2929e9483efc82c82c32ee7e02d563e&cnt=10"

# If expired, get new key from: https://openweathermap.org/api
# Update in application.properties
```

**Problem: Tests failing**
```bash
# Run tests with more details
mvn test -X

# Skip tests temporarily
mvn clean install -DskipTests

# Check test reports
open backend/target/surefire-reports/index.html
```

### Frontend Issues

**Problem: npm install fails**
```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and package-lock.json
rm -rf node_modules package-lock.json

# Retry install
npm install
```

**Problem: CORS errors**
```bash
# Check backend CORS settings in application.properties
weather.cors.allowed-origins=http://localhost:3000

# Verify backend is running
curl http://localhost:8080/api/v1/weather/health
```

**Problem: "Module not found" errors**
```bash
# Reinstall dependencies
rm -rf node_modules
npm install

# Check file structure matches imports
```

### Docker Issues

**Problem: Docker build fails**
```bash
# Check Docker is running
docker ps

# Build with no cache
docker-compose build --no-cache

# Check Docker logs
docker-compose logs
```

**Problem: Port conflicts**
```bash
# Change ports in docker-compose.yml
ports:
  - "8081:8080"  # Backend
  - "3001:80"    # Frontend
```

**Problem: Out of disk space**
```bash
# Clean up Docker
docker system prune -a
docker volume prune
```

---

## 🎯 Configuration for Different Environments

### Development
```properties
# application-dev.properties
spring.profiles.active=dev
logging.level.com.weather=DEBUG
weather.offline.enabled=false
```

### Testing
```properties
# application-test.properties  
spring.profiles.active=test
weather.offline.enabled=true  # Use offline data
```

### Production
```properties
# application-prod.properties
spring.profiles.active=prod
logging.level.com.weather=INFO
weather.api.key=ENC(encrypted-key)
server.ssl.enabled=true
```

Run with specific profile:
```bash
# Development
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production
java -jar -Dspring.profiles.active=prod target/app.jar
```

---

## 📊 Accessing Different Interfaces

Once running, access these URLs:

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:3000 | React UI |
| **Backend API** | http://localhost:8080/api/v1/weather | REST API |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Interactive API docs |
| **OpenAPI Spec** | http://localhost:8080/api-docs | API specification |
| **Health Check** | http://localhost:8080/actuator/health | Service health |
| **Metrics** | http://localhost:8080/actuator/metrics | Application metrics |

---

## 🧪 Test Data

### Cities with Offline Data
These cities have offline data available:
- London
- New York (as "newyork", no space)
- Tokyo

### Test Scenarios

**Scenario 1: Normal Weather**
```bash
curl "http://localhost:8080/api/v1/weather/forecast?city=London"
# Should show moderate temperatures, no warnings
```

**Scenario 2: High Temperature (>40°C)**
```bash
# Use offline mode and search for a city with high temps
curl -X POST "http://localhost:8080/api/v1/weather/offline-mode?enabled=true"
# Then search for cities in the offline data with temps > 40°C
```

**Scenario 3: Rain/Thunderstorm**
```bash
curl "http://localhost:8080/api/v1/weather/forecast?city=Seattle"
# Often shows rain warnings
```

---

## 📝 Next Steps

After successful setup:

1. **Explore the Code**:
   - Start with `WeatherPredictionApplication.java`
   - Follow the flow: Controller → Service → Provider
   - Understand the Strategy pattern in warning logic

2. **Run Tests**:
   - Backend: `mvn test`
   - Frontend: `npm test`
   - Review test coverage reports

3. **Customize**:
   - Add new warning strategies
   - Try different styling
   - Add new API endpoints

4. **Deploy**:
   - Build Docker images
   - Push to registry
   - Deploy to cloud (AWS, Azure, GCP)

---

## 💡 Tips for Interview Demo

1. **Before the Interview**:
   - Run the application multiple times
   - Practice explaining the architecture
   - Prepare to show different scenarios
   - Have backup plan (screenshots/video) if demo fails

2. **During Setup**:
   - Use Docker for reliability
   - Have application running before starting
   - Keep terminal windows organized
   - Have Swagger UI ready in a tab

3. **Demo Flow**:
   - Start with architecture diagram
   - Show live application
   - Walk through code
   - Demonstrate offline mode
   - Show test coverage
   - Explain CI/CD pipeline

4. **Be Ready to Show**:
   - Design patterns in code
   - Test cases
   - Docker configuration
   - Swagger documentation
   - Error handling

---

## 🆘 Getting Help

If you encounter issues:

1. **Check Logs**:
   ```bash
   # Backend logs
   tail -f backend/logs/application.log
   
   # Docker logs
   docker-compose logs -f
   ```

2. **Verify Dependencies**:
   ```bash
   # Java version
   java -version
   
   # Maven version
   mvn -version
   
   # Node version
   node -version
   ```

3. **Check Network**:
   ```bash
   # Test OpenWeather API
   curl "https://api.openweathermap.org/data/2.5/forecast?q=London&appid=d2929e9483efc82c82c32ee7e02d563e"
   
   # Test local backend
   curl http://localhost:8080/api/v1/weather/health
   ```

4. **Review Documentation**:
   - README.md - Project overview
   - INTERVIEW_QA.md - Interview questions
   - This file - Setup instructions

---

## ✅ Pre-Interview Checklist

- [ ] Application runs successfully (Docker or local)
- [ ] Can search for different cities
- [ ] Offline mode toggle works
- [ ] Swagger UI accessible
- [ ] Tests pass (backend and frontend)
- [ ] Understand the architecture
- [ ] Can explain design patterns
- [ ] Know the tech stack
- [ ] Practiced the demo (3-5 times)
- [ ] Have backup plan ready

Good luck! 🚀
