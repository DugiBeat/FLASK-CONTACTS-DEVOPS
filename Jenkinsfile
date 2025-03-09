pipeline {
    agent any
    
    parameters {
        string(name: 'APP_NAMESPACE', defaultValue: 'default', description: 'Kubernetes namespace for application')
    }
    
    stages {
        stage('Clone Repository') {
            steps {
                echo 'Cloning the GitHub repository...'
                git url: 'https://github.com/DugiBeat/FLASK-CONTACTS-DEVOPS.git', branch: 'master'
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                sh "kubectl create namespace ${params.APP_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f --validate=false"
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
            echo "✅ Deployment successful! Your application is now running in the ${params.APP_NAMESPACE} namespace."
        }
        failure {
            echo "❌ Deployment failed. Check the logs for more information."
        }
    }
}
