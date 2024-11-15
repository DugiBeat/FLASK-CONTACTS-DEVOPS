pipeline {
    agent {
        kubernetes {
           label 'helm-pod'
        }
    }
    stages {
       
        
        stage('check helm and kubectl') {
            steps {
                container('helm-pod') {
                    sh 'helm ls -A'
                    sh 'kubectl get deployments -n devops-tools'
                }
            }
        }
        stage('install prometheus') {
            steps {
                container('helm-pod') {
                    sh 'helm repo add prometheus-community https://prometheus-community.github.io/helm-charts'
                    sh 'helm repo update'
                    sh 'helm install prometheus  prometheus-community/kube-prometheus-stack'
                }
            }
        }
    }
}