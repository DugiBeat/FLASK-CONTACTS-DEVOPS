pipeline {
 agent any
    
 environment {
        AWS_REGION = 'eu-north-1'
        AWS_ACCESS_KEY = credentials('AWS_KEY')
        AWS_SECRET_KEY = credentials('AWS_S_KEY')
        CLUSTER_NAME = 'eks_mause'
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
 
         stage('Configure AWS') {
            steps {
                sh '''
                mkdir -p ~/.aws
                echo "[default]" > ~/.aws/credentials
                echo "aws_access_key_id=${AWS_ACCESS_KEY}" >> ~/.aws/credentials
                echo "aws_secret_access_key=${AWS_SECRET_KEY}" >> ~/.aws/credentials
                echo "[default]" > ~/.aws/config
                echo "region=${AWS_REGION}" >> ~/.aws/config
                '''
            }
        }
        
        stage('Configure kubectl') {
            steps {
                sh 'kubectl apply -f k8s/aws-auth.yaml --validate=false'
                sh 'aws eks update-kubeconfig --region ${AWS_REGION} --name ${CLUSTER_NAME}'
                sh 'kubectl config view'
                sh 'kubectl get nodes'
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
        
        stage('Update Kubernetes Manifests') {
            steps {
                // Optional: Update image tag in manifests
                sh '''
                sed -i "s|image: ${ECR_REPOSITORY_URI}:.*|image: ${ECR_REPOSITORY_URI}:${IMAGE_TAG}|g" k8s/deployment.yaml
                '''
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                sh 'kubectl apply -f k8s/'
                sh 'kubectl get pods,svc'
            }
        }
    }
    
    post {
        success {
            echo "✅ Pipeline completed successfully! Application deployed to existing cluster."
        }
        failure {
            echo "❌ Pipeline failed. Check the logs for more information."
        }
    }
}
