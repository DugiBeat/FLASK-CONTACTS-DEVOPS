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
        stage('install mongo') {
            steps {
                container('helm-pod') {
                    sh '''
kubectl apply -f - <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mongo
  labels:
    app: mongo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mongo
  template:
    metadata:
      labels:
        app: mongo
    spec:
      containers:
      - name: mongo
        image: mongo:4.4.6
        ports:
        - containerPort: 27017
---
apiVersion: v1
kind: Service
metadata:
  name: mongo-service
spec:
  type: ClusterIP
  selector:
    app: mongo
  ports:
    - protocol: TCP
      port: 27017
      targetPort: 27017
EOF
'''

                }
            }
        }
        stage('install mongo-exporter') {
            steps {
                container('helm-pod') {
                    script {
                        // Helm install command with custom values
                        sh """
                        helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
                        helm repo update
    
                        helm install mongo-exporter prometheus-community/prometheus-mongodb-exporter \
                            --set mongodb.uri="mongodb://mongo-service:27017" \
                            --set serviceMonitor.enabled=true \
                            --set serviceMonitor.additionalLabels.release=prometheus \
                            --namespace jenkins
                        """
                    }
                }
            }
        }
    }
}