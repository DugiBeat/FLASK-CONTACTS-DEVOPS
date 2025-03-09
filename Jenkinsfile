pipeline {
    agent any
    
    parameters {
        string(name: 'APP_NAMESPACE', defaultValue: 'default', description: 'Kubernetes namespace for application')
    }
    
    environment {
        KUBECONFIG = credentials('KUBE_CONFIG') // 🔹 Uses stored kubeconfig file in Jenkins
    }
    
    stages {
        stage('Clone Repository') {
            steps {
                echo '🔹 Cloning the GitHub repository...'
                git url: 'https://github.com/DugiBeat/FLASK-CONTACTS-DEVOPS.git', branch: 'master'
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    echo '🔹 Checking if namespace exists...'
                    def namespaceCheck = sh(script: "kubectl get namespace ${params.APP_NAMESPACE} || echo 'missing'", returnStdout: true).trim()
                    
                    if (namespaceCheck.contains("missing")) {
                        echo "🔹 Creating namespace: ${params.APP_NAMESPACE}"
                        sh "kubectl create namespace ${params.APP_NAMESPACE}"
                    } else {
                        echo "✅ Namespace ${params.APP_NAMESPACE} already exists."
                    }
                }
                
                echo '🔹 Deploying application to Kubernetes...'
                def files = [
                    "alertmaneger-deployment.yaml",
                    "app-deployment.yaml",
                    "app-service.yaml",
                    "grafana-deployment.yaml",
                    "grafana-service.yaml",
                    "prometheus-config.yaml",
                    "prometheus-deployment.yaml",
                    "prometheus-service-mon.yaml",
                    "prometheus-service.yaml"
                ]
                
                for (file in files) {
                    sh "kubectl apply -f k8s/${file} -n ${params.APP_NAMESPACE}"
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                echo '🔹 Verifying deployment status...'
                sh "kubectl get pods,svc,deployments -n ${params.APP_NAMESPACE}"
            }
        }
    }
    
    post {
        success {
            echo "✅ Deployment successful! Your application is running in the '${params.APP_NAMESPACE}' namespace."
        }
        failure {
            echo "❌ Deployment failed. Check Kubernetes logs for more details."
        }
    }
}
