pipeline {
    agent {
        kubernetes {
           label 'helm-pod'
        }
    }
    stages {
       
        
        stage('helm and kubectl versions') {
            steps {
                container('helm-pod') {
                    sh 'helm ls -A'
                    sh 'kubectl get deployments -n devops-tools'
                }
            }
        }
    }
}