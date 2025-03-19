pipeline {
    agent any

    environment {
        AWS_REGION = 'eu-north-1'
        AWS_ACCESS_KEY = credentials('AWS_KEY')
        AWS_SECRET_KEY = credentials('AWS_S_KEY')
        ECR_REPOSITORY_URI = '423623847692.dkr.ecr.eu-north-1.amazonaws.com/finaldevop/dugems'  
        IMAGE_TAG = "${env.BUILD_NUMBER}"
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
                    export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY}
                    export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_KEY}
                    export AWS_DEFAULT_REGION=${AWS_REGION}
                    
                    aws sts get-caller-identity
                    """
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${ECR_REPOSITORY_URI}:${IMAGE_TAG} ."
                sh "docker tag ${ECR_REPOSITORY_URI}:${IMAGE_TAG} ${ECR_REPOSITORY_URI}:latest"
            }
        }

        stage('Push to ECR') {
            steps {
                sh '''
                aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPOSITORY_URI}
                docker push ${ECR_REPOSITORY_URI}:${IMAGE_TAG}
                docker push ${ECR_REPOSITORY_URI}:latest
                '''
            }
        }

        stage('Run Docker Container Locally') {
            steps {
                sh '''
                docker stop flask-container || true
                docker rm flask-container || true
                docker run -d --name flask-container -p 5000:5000 ${ECR_REPOSITORY_URI}:${IMAGE_TAG}
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
