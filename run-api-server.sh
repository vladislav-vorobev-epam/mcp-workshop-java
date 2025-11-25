#!/bin/bash
# Run the Tasks REST API server on port 8001

echo "=========================================="
echo "Starting Tasks REST API Server"
echo "=========================================="
echo "Server will run on: http://localhost:8001"
echo "API Base URL: http://localhost:8001/api"
echo "Press Ctrl+C to stop the server"
echo "=========================================="
echo ""

mvn spring-boot:run
