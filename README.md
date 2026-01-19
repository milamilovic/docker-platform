# docker-platform
Project for class Managing Software Configuration

## Elasticsearch - first setup
Before running the backend application for the first time, start the elasticsearch container (`docker compose up -d elasticsearch`) and reset the password for the `elastic` user:
    
```bash
docker exec -it elasticsearch bin/elasticsearch-reset-password -u elastic
```

Update your .env file with the new generated password and restart the elasticsearch container.
