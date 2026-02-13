# docker-platform

A platform for hosting docker repositories, project for class Managing Software Configuration

## Images
Home page
<br/>
<img width="800" alt="Home page" src="https://github.com/user-attachments/assets/f9fe7bfe-cdb7-4e5b-a5cc-c04e12d86530" />
<br/><br/>
Repository and tags detailed view
<br/>
<img width="800" alt="Repository details" src="https://github.com/user-attachments/assets/e9f5c3f5-44a7-41da-b3ad-9472a289229e" />
<br/><br/>
Sponsored OSS repository list
<br/>
<img width="800" alt="Sponsored OSS" src="https://github.com/user-attachments/assets/2683f5b0-48c1-43b3-9cd0-164f48bbfe29" />
<br/><br/>
Analytics search bar
<br/>
<img width="800" alt="Analytics search bar" src="https://github.com/user-attachments/assets/7474f25f-a466-4601-90a6-bab176aeaf3e" />
<br/><br/>
Analytics log results
<br/>
<img width="800" alt="Log search results" src="https://github.com/user-attachments/assets/6e48f90e-affd-4123-b353-26f85915fee5" />
<br/><br/>


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
