pipeline {
    agent any

    environment {
        VIRTUAL_ENV = 'venv'  // Virtual environment for Python
    }

    stages {

        stage('Clone Repository') {
            steps {
                echo 'Cloning the GitHub repository...'
                git 'https://github.com/DugiBeat/FLASK-CONTACTS-DEVOPS.git'  // Replace with your actual repo
            }
        }

        stage('Setup Python Environment') {
            steps {
                echo 'Setting up Python environment...'
                sh '''
                    # Install Python if not installed
                    sudo apt update
                    sudo apt install -y python3 python3-venv python3-pip

                    # Create and activate virtual environment
                    python3 -m venv $VIRTUAL_ENV
                    source $VIRTUAL_ENV/bin/activate

                    # Install dependencies
                    pip install -r requirements.txt
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
                    nohup python app.py &  # Run the app in the background
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                echo 'Deploying to Kubernetes...'
                sh '''
                    kubectl apply -f k8s/deployment.yaml
                    kubectl apply -f k8s/service.yaml
                '''
            }
        }

    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
        }
    }
}
