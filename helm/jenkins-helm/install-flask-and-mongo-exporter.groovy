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
    
                        helm upgrade --install mongo-exporter prometheus-community/prometheus-mongodb-exporter \
                            --set mongodb.uri="mongodb://mongo-service:27017" \
                            --set serviceMonitor.enabled=true \
                            --set serviceMonitor.additionalLabels.release=prometheus \
                            --namespace jenkins
                        """
                    }
                }
            }
        }
        stage('install flask for mongo') {
            steps {
                container('helm-pod') {
                    sh '''
kubectl apply -f - <<EOF
apiVersion: v1
kind: ConfigMap
metadata:
  name: flask-mongo-config
data:
  # db_url: "mysql-service"
  db_type: "MONGO"
  mongo_url: "mongodb://mongo-service:27017"
  mongodb_host: "mongo-service"
  # my.cnf: | # This is the configuration file for MySQL
  #   [mysqld]
  #   transaction-isolation = READ-COMMITTED
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flask-contacts-app
  labels:
    app: flask-contacts-app
spec:
  replicas: 2
  selector:
    matchLabels:
      app: flask-contacts-app
  template:
    metadata:
      labels:
        app: flask-contacts-app
    spec:
      containers:
      - name: flask-contacts-app
        image: shashkist/flask-contacts-app
        ports:
        - containerPort: 5052
        env:
        - name: DATABASE_TYPE
          valueFrom:
            configMapKeyRef:
              name: flask-mongo-config
              key: db_type
        - name: MONGO_URI
          valueFrom:
            configMapKeyRef:
              name: flask-mongo-config
              key: mongo_url
---
apiVersion: v1
kind: Service
metadata:
  name: flask-contacts-app-service
  labels:
    app: flask-contacts-service # this should be mapped by service monitor
spec:
  selector:
    app: flask-contacts-app
  type: LoadBalancer  
  ports:
    - name: flask-contacts-service #this port name should be mapped by service monitor
      protocol: TCP
      port: 5053
      targetPort: 5052
EOF
'''
                }
            }
        }
         stage('add service monitor for flask') {
            steps {
                container('helm-pod') {
                     sh '''
kubectl apply -f - <<EOF
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: flask-service-monitor
  labels:
    # app: flask-contacts-app
    release: prometheus
spec:
  endpoints:
  - interval: 30s
    port: flask-contacts-service
    scrapeTimeout: 10s
  selector:
    matchLabels:
      app: flask-contacts-service 
EOF
'''
                }
            }
         }
        
    }
}