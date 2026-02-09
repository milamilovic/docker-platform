# docker-platform

Project for class Managing Software Configuration

## First Time Setup

### 1. Clone the repository
```bash
git clone 
cd docker-platform
```

### 2. Environment configuration

Create a `.env` file in the root directory. You can use `.env.example` as a template.

### 3. Elasticsearch password setup

Before running the full application, you need to set up Elasticsearch credentials:
```bash
docker compose up -d elasticsearch
```

Wait a minute for Elasticsearch to start, then reset the password:
```bash
docker exec -it elasticsearch bin/elasticsearch-reset-password -u elastic
```

Copy the generated password and update `ELASTIC_PASSWORD` in your `.env` file.

Stop the elasticsearch container:
```bash
docker compose down
```

## Running the Application

Start all services:
```bash
docker compose up --build
```

This will start:
- PostgreSQL database (port 5432)
- Redis cache (port 6379)
- Elasticsearch (port 9200)
- Logstash (port 5000)
- Backend API (port 8080)
- Frontend (port 4200)
- Docker Registry (port 5000)

You can now access the application on http://localhost:4200


## Stopping the Application
```bash
docker compose down
```

To remove volumes (this will delete all data, both from the databases and from elasticsearch (you will need to generate the passord again)):
```bash
docker compose down -v
```