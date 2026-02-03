pipeline {
    agent any
    
    tools {
        maven 'Maven 3.9'
        jdk 'JDK 17'
        nodejs 'NodeJS 18'
    }
    
    environment {
        DOCKER_REGISTRY = 'docker.io'
        IMAGE_NAME = 'weather-prediction'
        IMAGE_TAG = "${BUILD_NUMBER}"
        SONAR_PROJECT_KEY = 'weather-prediction'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from repository...'
                stage('Checkout') {
                    steps {
                        echo 'Checking out code from GitHub...'
                        git branch: 'main',
                            url: 'https://github.com/<your-username>/<your-repo>.git'
                    }
                }
            }
        }
        
        stage('Build Backend') {
            steps {
                echo 'Building Spring Boot application...'
                dir('backend') {
                    sh 'mvn clean compile'
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                echo 'Running unit tests...'
                dir('backend') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: 'backend/target/jacoco.exec',
                        classPattern: 'backend/target/classes',
                        sourcePattern: 'backend/src/main/java'
                    )
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                echo 'Running integration tests...'
                dir('backend') {
                    sh 'mvn verify -DskipUnitTests'
                }
            }
        }
        
        stage('Code Quality Analysis') {
            steps {
                echo 'Running code quality checks...'
                dir('backend') {
                    // SonarQube analysis (configure SonarQube server in Jenkins)
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.login=${SONAR_TOKEN}
                    '''
                }
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
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }
        
        stage('Frontend Tests') {
            steps {
                echo 'Running frontend tests...'
                dir('frontend') {
                    sh 'npm test -- --coverage --watchAll=false'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                echo 'Building Docker images...'
                script {
                    // Build backend image
                    dir('backend') {
                        sh "docker build -t ${IMAGE_NAME}-backend:${IMAGE_TAG} ."
                        sh "docker tag ${IMAGE_NAME}-backend:${IMAGE_TAG} ${IMAGE_NAME}-backend:latest"
                    }
                    
                    // Build frontend image
                    dir('frontend') {
                        sh "docker build -t ${IMAGE_NAME}-frontend:${IMAGE_TAG} ."
                        sh "docker tag ${IMAGE_NAME}-frontend:${IMAGE_TAG} ${IMAGE_NAME}-frontend:latest"
                    }
                }
            }
        }
        
        stage('Security Scan') {
            steps {
                echo 'Running security scans...'
                script {
                    // Trivy vulnerability scanner
                    sh "trivy image --severity HIGH,CRITICAL ${IMAGE_NAME}-backend:${IMAGE_TAG}"
                    sh "trivy image --severity HIGH,CRITICAL ${IMAGE_NAME}-frontend:${IMAGE_TAG}"
                }
            }
        }
        
        stage('Push to Registry') {
            when {
                branch 'main'
            }
            steps {
                echo 'Pushing Docker images to registry...'
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-credentials') {
                        sh "docker push ${IMAGE_NAME}-backend:${IMAGE_TAG}"
                        sh "docker push ${IMAGE_NAME}-backend:latest"
                        sh "docker push ${IMAGE_NAME}-frontend:${IMAGE_TAG}"
                        sh "docker push ${IMAGE_NAME}-frontend:latest"
                    }
                }
            }
        }
        
        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying application...'
                script {
                    // Deploy using docker-compose
                    sh 'docker-compose down'
                    sh 'docker-compose up -d'
                }
            }
        }
        
        stage('Health Check') {
            steps {
                echo 'Performing health checks...'
                script {
                    sleep(30) // Wait for services to start
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
            // Send notification (email, Slack, etc.)
        }
        failure {
            echo 'Pipeline failed!'
            // Send failure notification
        }
    }
}
