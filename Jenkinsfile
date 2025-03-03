pipeline {
    agent any

    environment {
        VIRTUAL_ENV = 'venv'  
        PROMETHEUS_VERSION = '2.46.0'  
        PROMETHEUS_CONFIG = '/etc/prometheus/prometheus.yml'
    }

    stages {

        stage('Clone Repository') {
            steps {
                echo 'Cloning the GitHub repository...'
                git 'https://github.com/DugiBeat/FLASK-CONTACTS-DEVOPS.git' 
            }
        }

        stage('Setup Python Environment') {
            steps {
                echo 'Setting up Python environment...'
                sh '''
                    sudo apt update -y
                    sudo apt install -y python3 python3-venv python3-pip
                    python3 -m venv $VIRTUAL_ENV
                    source $VIRTUAL_ENV/bin/activate
                    pip install -r requirements.txt
                '''
            }
        }

        stage('Install & Configure Prometheus') {
            steps {
                echo 'Installing Prometheus...'
                sh '''
                    cd /tmp
                    wget https://github.com/prometheus/prometheus/releases/download/v$PROMETHEUS_VERSION/prometheus-$PROMETHEUS_VERSION.linux-amd64.tar.gz
                    tar -xvf prometheus-$PROMETHEUS_VERSION.linux-amd64.tar.gz
                    sudo mv prometheus-$PROMETHEUS_VERSION.linux-amd64 /opt/prometheus

                    sudo useradd --no-create-home --shell /bin/false prometheus
                    sudo mkdir /etc/prometheus
                    sudo mkdir /var/lib/prometheus
                    sudo chown prometheus:prometheus /etc/prometheus /var/lib/prometheus
                '''
            }
        }

        stage('Configure Prometheus to Scrape Jenkins & App') {
            steps {
                echo 'Configuring Prometheus...'
                sh '''
                    echo "global:
                      scrape_interval: 15s

                    scrape_configs:
                      - job_name: 'jenkins'
                        static_configs:
                          - targets: ['localhost:8080']

                      - job_name: 'application'
                        static_configs:
                          - targets: ['localhost:5000']  # Change to your app port
                    " | sudo tee $PROMETHEUS_CONFIG

                    sudo systemctl restart prometheus
                '''
            }
        }

        stage('Run Database Migrations') {
            steps {
                echo 'Running database migrations...'
                sh '''
                    source $VIRTUAL_ENV/bin/activate
                    python migrate.py
                '''
            }
        }

        stage('Run Application') {
            steps {
                echo 'Starting the application...'
                sh '''
                    source $VIRTUAL_ENV/bin/activate
                    nohup python app.py &  # Run in background
                '''
            }
        }

        stage('Verify Prometheus Scraping') {
            steps {
                echo 'Checking Prometheus Targets...'
                sh '''
                    curl -s http://localhost:9090/api/v1/targets | jq .
                '''
            }
        }

    }

    post {
        success {
            echo 'Pipeline completed successfully! Prometheus is scraping Jenkins & the application.'
        }
        failure {
            echo 'Pipeline failed. Check Prometheus logs.'
        }
    }
}
