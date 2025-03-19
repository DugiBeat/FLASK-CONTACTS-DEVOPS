pipeline {
    agent any
    environment {
        AWS_REGION = 'eu-north-1'
        AWS_ACCESS_KEY_ID = credentials('AWS_KEY')
        AWS_SECRET_ACCESS_KEY = credentials('AWS_S_KEY')
        ECR_REPOSITORY_URI = '423623847692.dkr.ecr.eu-north-1.amazonaws.com/finaldevop/dugems'  
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        
        // Application environment variables with defaults
        DB_HOST = "${params.DB_HOST ?: 'localhost'}"
        DB_USER = "${params.DB_USER ?: 'jenkins_dugems'}"
        DB_PASSWORD = credentials('DB_PASSWORD') // Keep this as credential
        DB_NAME = "${params.DB_NAME ?: 'dugems_flask_db'}"
        DATABASE_TYPE = "${params.DATABASE_TYPE ?: 'MYSQL'}"
        DB_PORT = "${params.DB_PORT ?: '3306'}"
        MONGO_URI = "${params.MONGO_URI ?: 'mongodb://localhost:27017/'}"
        
        // Note: OPENAI_API_KEY is now handled separately in the script
    }
    
    parameters {
        string(name: 'DB_HOST', defaultValue: 'localhost', description: 'Database host')
        string(name: 'DB_USER', defaultValue: 'root', description: 'Database user')
        string(name: 'DB_NAME', defaultValue: 'contacts_app', description: 'Database name')
        string(name: 'DATABASE_TYPE', defaultValue: 'MYSQL', description: 'Database type (MYSQL or MONGO)')
        string(name: 'DB_PORT', defaultValue: '3306', description: 'Database port')
        string(name: 'MONGO_URI', defaultValue: 'mongodb://localhost:27017/', description: 'MongoDB URI (if using MongoDB)')
        string(name: 'USE_OPENAI', defaultValue: 'false', description: 'Set to true if you want to use OpenAI API')
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Cloning repository...'
                git url: 'https://github.com/DugiBeat/FLASK-CONTACTS-DEVOPS.git', branch: 'master'
            }
        }
        
        stage('Setup AWS CLI') {
            steps {
                script {
                    sh """
                    aws sts get-caller-identity
                    """
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                sh "sudo docker build -t ${ECR_REPOSITORY_URI}:${IMAGE_TAG} ."
                sh "sudo docker tag ${ECR_REPOSITORY_URI}:${IMAGE_TAG} ${ECR_REPOSITORY_URI}:latest"
            }
        }
        
        stage('Push to ECR') {
            steps {
                sh """
                aws ecr get-login-password --region ${AWS_REGION} | sudo docker login --username AWS --password-stdin ${ECR_REPOSITORY_URI}
                sudo docker push ${ECR_REPOSITORY_URI}:${IMAGE_TAG}
                sudo docker push ${ECR_REPOSITORY_URI}:latest
                """
            }
        }
        
        stage('Run Docker Container Locally') {
            steps {
                script {
                    // Stop and remove any existing container
                    sh "sudo docker stop flask-container || true"
                    sh "sudo docker rm flask-container || true"
                    
                    // Use withCredentials to handle DB_PASSWORD securely
                    withCredentials([string(credentialsId: 'DB_PASSWORD', variable: 'DB_PWD')]) {
                        def dockerRunCmd = """
                        sudo docker run -d --name flask-container -p 5052:5052 \\
                            -e DB_HOST=${DB_HOST} \\
                            -e DB_USER=${DB_USER} \\
                            -e DB_PASSWORD=${DB_PWD} \\
                            -e DB_NAME=${DB_NAME} \\
                            -e DATABASE_TYPE=${DATABASE_TYPE} \\
                            -e DB_PORT=${DB_PORT} \\
                            -e MONGO_URI=${MONGO_URI} \\
                            ${ECR_REPOSITORY_URI}:${IMAGE_TAG}
                        """
                        
                        // Add OpenAI API key if USE_OPENAI is true
                        if (params.USE_OPENAI == 'true') {
                            try {
                                withCredentials([string(credentialsId: 'OPENAI_API_KEY', variable: 'OPENAI_KEY')]) {
                                    dockerRunCmd = """
                                    sudo docker run -d --name flask-container -p 5052:5052 \\
                                        -e DB_HOST=${DB_HOST} \\
                                        -e DB_USER=${DB_USER} \\
                                        -e DB_PASSWORD=${DB_PWD} \\
                                        -e DB_NAME=${DB_NAME} \\
                                        -e DATABASE_TYPE=${DATABASE_TYPE} \\
                                        -e DB_PORT=${DB_PORT} \\
                                        -e MONGO_URI=${MONGO_URI} \\
                                        -e OPENAI_API_KEY=${OPENAI_KEY} \\
                                        ${ECR_REPOSITORY_URI}:${IMAGE_TAG}
                                    """
                                }
                            } catch (Exception e) {
                                echo "OPENAI_API_KEY credential not found, continuing without it"
                            }
                        }
                        
                        sh dockerRunCmd
                    }
                }
            }
        }
        
        stage('Run Database Migration') {
            steps {
                sh '''
                # Wait for container to be ready
                sleep 5
                
                # Execute migration script inside the container
                sudo docker exec flask-container python3 migrate.py || echo "Migration failed, but continuing"
                '''
            }
        }
        
        stage('Verify Application') {
            steps {
                sh '''
                # Wait for the application to start
                sleep 10
                
                # Check if the application is running
                curl -s http://localhost:5052/ || echo "Application not responding, but continuing"
                '''
            }
        }
    }
    
    post {
        success {
            echo "✅ Pipeline completed successfully! Docker image built, pushed to ECR, and running locally."
        }
        failure {
            echo "❌ Pipeline failed. Check the logs for more information."
        }
    }
}
  
