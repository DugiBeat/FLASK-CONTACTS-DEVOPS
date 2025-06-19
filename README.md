# Dugma - DevOps & Cybersecurity Dashboard 🛡️🚀

A full-stack Flask-based web app designed to combine contact management, cybersecurity utilities, and DevOps observability in a deployable CI/CD pipeline.  
Built to run locally via Docker or on cloud via Kubernetes (Helm) with Jenkins, Prometheus, and Grafana.

---

## 📚 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Installation](#installation)
- [Environment Variables](#environment-variables)
- [Usage](#usage)
- [Routes](#routes)
- [Screenshots](#screenshots)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Monitoring](#monitoring)
- [License](#license)
- [Contact](#contact)

---

## 🌟 Features

- 📇 Contact management (CRUD) with MySQL or MongoDB
- 🧠 Domain scanner using WHOIS lookup
- 📅 Cybersecurity meeting scheduler
- ✅ Meeting status updates (Pending / Approved / Rejected)
- 🚨 CVE alerts from NIST's National Vulnerability Database
- 🌐 Modern DevSecOps UI with floating sidebar navigation
- 💡 Welcome popup and search/sortable tables
- 🐳 Dockerized setup for local use
- ☸️ Helm charts for Kubernetes deployments (AWS EKS-ready)
- ⚙️ Jenkins CI/CD pipeline compatible
- 📈 Monitoring via Prometheus + Grafana
- 📤 CSV export of meetings

---

## ⚙️ Tech Stack

- **Backend:** Flask + Jinja2
- **Frontend:** HTML/CSS + Vanilla JS
- **Database:** MySQL (default) or MongoDB
- **CI/CD:** Jenkins
- **Infra:** Docker, Helm, Terraform, Kubernetes (EKS)
- **Monitoring:** Prometheus + Grafana

---

## 📦 Installation

### Python + Virtual Environment (Debian/Ubuntu)

```bash
sudo apt update
sudo apt install python3 python3-pip python3-venv
```

### Create a Virtual Environment (Optional)
```bash
python3 -m venv .venv
source .venv/bin/activate
```
Install Python Requirements
```bash
pip install -r requirements.txt
```

🔐 Environment Variables
Create a .env file in your root directory with the following:

```env
# Required
DATABASE_TYPE=MYSQL          # or MONGO
DB_HOST=mysql
DB_PORT=3306
DB_USER=root
DB_PASSWORD=admin
DB_NAME=contacts_app

# Optional
MONGO_URI=mongodb://localhost:27017/
OPENAI_API_KEY=your_openai_api_key
To load them manually in Linux:
```

```bash
export DB_USER=root
export DB_PASSWORD=admin
# etc...
```

🛠️ Usage
1. Initialize the MySQL Database
```bash
python3 migrate.py
Creates the contacts and meetings tables and inserts sample data.
```

2. Run Flask Application
```bash
python3 app.py
```

App is now live at: http://localhost:5052

🔗 Routes
UI Views
/ → Redirects to /viewContacts

/viewContacts → Dashboard (Contacts + Meetings)

/addContact → Add new contact form

/editContact/<id> → Edit existing contact

/book-meeting → Book cybersecurity consultation

/crawl → Domain WHOIS scanner

/alerts → CVE alert dashboard

API Endpoints
GET /api/contacts

GET /api/contact/<number>

POST /api/contact

PUT /api/contact/<number>

DELETE /api/contact/<number>

POST /api/scan

POST /api/book-meeting

GET /api/bookings

PUT /api/bookings/<id>/status

GET /api/alerts

🖼️ Screenshots
Replace with your own if needed






☸️ Kubernetes Deployment
1. Helm Chart
Helm files located under:

bash
Copy
Edit
/helm/dugma-webapp/
To install:

bash
Copy
Edit
helm install dugma ./helm/dugma-webapp
2. Jenkins Pipeline (Optional)
Connect Jenkinsfile to a GitHub repo with webapp and helm folders

Configure Jenkins to:

Build Docker image and push to ECR

Deploy Helm chart to EKS using kubectl

📊 Monitoring (Optional)
Prometheus + Grafana monitoring stack can be deployed via Helm:

bash
Copy
Edit
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install monitoring prometheus-community/kube-prometheus-stack
Grafana dashboards can be configured for app metrics once exposed via Prometheus exporter.

🐳 Docker Usage
Build & Run:

bash
Copy
Edit
docker-compose up --build
Tears down:

bash
Copy
Edit
docker-compose down -v
Ensure Dockerfile, .env, migrate.py, and wait-for-mysql.sh are included.

💾 CSV Export
The meetings table on the dashboard supports CSV export.
Click the export button to download all bookings in CSV format.

📝 License
MIT License – You are free to use, modify, and distribute this project with attribution.

📬 Contact
Created by Dugi Dug
📧 Email: dugma@example.com
💼 LinkedIn: [Your Profile]
📦 GitHub: https://github.com/your-profile

Powered by Flask + DevOps tools: Jenkins, Terraform, Helm, and AWS EKS.
