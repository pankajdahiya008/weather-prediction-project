pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
        jdk 'OpenJDK-17'
    }

    environment {
        DOCKER_REGISTRY = 'docker.io'
        IMAGE_NAME = 'weather-prediction'
        IMAGE_TAG = "${BUILD_NUMBER}"
        SONAR_PROJECT_KEY = 'weather-prediction'
        MAVEN_OPTS = '-DskipTests' // Skip all backend tests globally
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from GitHub...'
                git branch: 'main',
                    url: 'https://github.com/pankajdahiya008/weather-prediction-project.git'
            }
        }

        stage('Build Backend') {
            steps {
                echo 'Building Spring Boot application...'
                dir('backend') {
                    sh 'mvn clean compile -DskipTests'
                }
            }
        }

        stage('Unit Tests') {
            steps {
                echo 'Skipping unit tests...'
                dir('backend') {
                    sh 'mvn test -DskipTests || true'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                echo 'Skipping integration tests...'
                dir('backend') {
                    sh 'mvn verify -DskipTests || true'
                }
            }
        }

        stage('Code Quality Analysis') {
            steps {
                echo 'Skipping SonarQube analysis for now'
                // sh 'mvn sonar:sonar -Dsonar.projectKey=${SONAR_PROJECT_KEY} -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_TOKEN}'
            }
        }

        stage('Package Backend') {
            steps {
                echo 'Packaging Spring Boot application...'
                dir('backend') {
                    sh 'mvn package -DskipTests'
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'backend/target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build Frontend') {
            steps {
                echo 'Building React application...'
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Frontend Tests') {
            steps {
                echo 'Running frontend tests...'
                dir('frontend') {
                    sh 'npm test -- --coverage --watchAll=false --passWithNoTests'
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker images...'
                script {
                    dir('backend') {
                        sh "docker build -t ${IMAGE_NAME}-backend:${IMAGE_TAG} ."
                        sh "docker tag ${IMAGE_NAME}-backend:${IMAGE_TAG} ${IMAGE_NAME}-backend:latest"
                    }
                    dir('frontend') {
                        sh "docker build -t ${IMAGE_NAME}-frontend:${IMAGE_TAG} ."
                        sh "docker tag ${IMAGE_NAME}-frontend:${IMAGE_TAG} ${IMAGE_NAME}-frontend:latest"
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying application...'
                script {
                    sh 'docker-compose down'
                    sh 'docker-compose up -d'
                }
            }
        }

        stage('Health Check') {
            steps {
                echo 'Performing health checks...'
                script {
                    sleep(30)
                    sh 'curl -f http://localhost:8080/actuator/health || exit 1'
                    sh 'curl -f http://localhost:3000 || exit 1'
                }
            }
        }
    }

    post {
        always {
            echo 'Cleaning up...'
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}