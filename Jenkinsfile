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
                git url: 'https://github.com/DugiBeat/FLASK-CONTACTS-DEVOPS.git', branch: 'master'
            }
        }

        stage('Setup Python Environment') {
            steps {
                echo 'Setting up Python environment...'
                sh '''#!/bin/bash
                set -e  # Exit immediately if a command fails

                echo "Updating system and installing dependencies..."
                sudo apt update -y
                sudo apt install -y python3 python3-venv python3-pip

                echo "Creating Python virtual environment..."
                python3 -m venv "$WORKSPACE/$VIRTUAL_ENV"

                echo "Activating virtual environment and installing dependencies..."
                bash -c "source $WORKSPACE/$VIRTUAL_ENV/bin/activate && pip install -r requirements.txt"
                '''
            }
        }

        stage('Install & Configure Prometheus') {
            steps {
                echo 'Installing Prometheus...'
                sh '''#!/bin/bash
                set -e

                echo "Downloading Prometheus..."
                cd /tmp
                wget https://github.com/prometheus/prometheus/releases/download/v$PROMETHEUS_VERSION/prometheus-$PROMETHEUS_VERSION.linux-amd64.tar.gz

                echo "Extracting Prometheus..."
                tar -xvf prometheus-$PROMETHEUS_VERSION.linux-amd64.tar.gz
                sudo mv prometheus-$PROMETHEUS_VERSION.linux-amd64 /opt/prometheus

                echo "Creating Prometheus user and setting permissions..."
                sudo useradd --no-create-home --shell /bin/false prometheus || true
                sudo mkdir -p /etc/prometheus /var/lib/prometheus
                sudo chown prometheus:prometheus /etc/prometheus /var/lib/prometheus /opt/prometheus
                '''
            }
        }

        stage('Run Application') {
            steps {
                echo 'Starting the Flask application...'
                sh '''#!/bin/bash
                set -e
                nohup bash -c "source $WORKSPACE/$VIRTUAL_ENV/bin/activate && python app.py" > app.log 2>&1 &
                '''
            }
        }

        stage('Run Database Migrations') {
            steps {
                echo 'Running database migrations...'
                sh '''#!/bin/bash
                set -e
                bash -c "source $WORKSPACE/$VIRTUAL_ENV/bin/activate && python migrate.py"
                '''
            }
        }
                stage('Configure Prometheus to Scrape Jenkins & App') {
            steps {
                echo 'Configuring Prometheus...'
                sh '''#!/bin/bash
                set -e

                echo "Configuring Prometheus scrape targets..."
                echo "global:
                  scrape_interval: 15s

                scrape_configs:
                  - job_name: 'jenkins'
                    static_configs:
                      - targets: ['localhost:8080']

                  - job_name: 'application'
                    static_configs:
                      - targets: ['localhost:5052']
                " | sudo tee $PROMETHEUS_CONFIG

                echo "Restarting Prometheus service..."
                sudo systemctl restart prometheus || sudo systemctl start prometheus
                '''
            }
        }
        
        stage('Verify Prometheus Scraping') {
            steps {
                echo 'Checking Prometheus targets...'
                sh '''#!/bin/bash
                set -e
                curl -s http://localhost:9090/api/v1/targets | jq .
                '''
            }
        }

    }

    post {
        success {
            echo '✅ Pipeline completed successfully! Prometheus is scraping Jenkins & the application.'
        }
        failure {
            echo '❌ Pipeline failed. Check Prometheus logs and system settings.'
        }
    }
}
