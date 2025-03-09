pipeline {
    agent any

    environment {
        AWS_REGION = 'eu-north-1'  // Change to your AWS region
        CLUSTER_NAME = 'eks_mause' // Replace with your EKS cluster name
        KUBECONFIG = '/var/lib/jenkins/.kube/config' // Path for kubeconfig
        AWS_ACCESS_KEY = credentials('AWS_KEY') // Uses Jenkins credential ID
        AWS_SECRET_KEY = credentials('AWS_S_KEY') // Uses Jenkins credential ID
    }

    stages {
        stage('Setup AWS CLI & Configure K8s') {
            steps {
                script {
                    // Install AWS CLI if not installed
                    sh '''
                    if ! command -v aws &> /dev/null; then
                        echo "Installing AWS CLI..."
                        sudo apt update && sudo apt install -y awscli
                    fi
                    '''

                    // Configure AWS CLI with Jenkins credentials
                    sh '''
                    mkdir -p ~/.aws
                    echo "[default]" > ~/.aws/credentials
                    echo "aws_access_key_id=${AWS_ACCESS_KEY}" >> ~/.aws/credentials
                    echo "aws_secret_access_key=${AWS_SECRET_KEY}" >> ~/.aws/credentials
                    echo "[default]" > ~/.aws/config
                    echo "region=${AWS_REGION}" >> ~/.aws/config
                    '''

                    // Install kubectl if not installed
                    sh '''
                    if ! command -v kubectl &> /dev/null; then
                        echo "Installing kubectl..."
                        curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
                        chmod +x kubectl
                        sudo mv kubectl /usr/local/bin/
                    fi
                    '''

                    // Update kubeconfig for AWS EKS
                    sh '''
                    echo "Updating kubeconfig..."
                    aws eks update-kubeconfig --region ${AWS_REGION} --name ${CLUSTER_NAME}
                    kubectl config view
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh "kubectl create namespace ${params.APP_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -"
                sh "kubectl apply -f k8s/ -n ${params.APP_NAMESPACE}"
            }
        }

        stage('Verify Deployment') {
            steps {
                sh "kubectl get pods,svc,deployments -n ${params.APP_NAMESPACE}"
            }
        }
    }

    post {
        success {
            echo "✅ Deployment successful!"
        }
        failure {
            echo "❌ Deployment failed. Check logs for details."
        }
    }
}
