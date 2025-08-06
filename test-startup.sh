#!/bin/bash

echo "🧪 Testing Application Startup"
echo "=============================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if we're in the right directory
if [ ! -f "docker-compose.yml" ]; then
    echo -e "${RED}❌ Please run this script from the project root directory${NC}"
    exit 1
fi

echo -e "\n${YELLOW}1. Checking Docker services...${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker is running${NC}"

# Stop any existing services
echo -e "\n${YELLOW}2. Stopping existing services...${NC}"
docker-compose down > /dev/null 2>&1

# Start services
echo -e "\n${YELLOW}3. Starting services...${NC}"
docker-compose up -d

# Wait for services to start
echo -e "\n${YELLOW}4. Waiting for services to be ready...${NC}"
sleep 30

# Check service status
echo -e "\n${YELLOW}5. Checking service status...${NC}"
docker-compose ps

# Test backend health
echo -e "\n${YELLOW}6. Testing backend health...${NC}"
if curl -f -s http://localhost:8080/actuator/health > /dev/null; then
    echo -e "${GREEN}✓ Backend is healthy${NC}"
else
    echo -e "${RED}❌ Backend health check failed${NC}"
    echo -e "\n${YELLOW}Backend logs:${NC}"
    docker-compose logs --tail=20 backend
fi

# Test frontend
echo -e "\n${YELLOW}7. Testing frontend...${NC}"
if curl -f -s http://localhost:3000 > /dev/null; then
    echo -e "${GREEN}✓ Frontend is accessible${NC}"
else
    echo -e "${RED}❌ Frontend is not accessible${NC}"
    echo -e "\n${YELLOW}Frontend logs:${NC}"
    docker-compose logs --tail=20 frontend
fi

# Test database
echo -e "\n${YELLOW}8. Testing database...${NC}"
if docker-compose exec -T postgres pg_isready -U postgres > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Database is ready${NC}"
else
    echo -e "${RED}❌ Database is not ready${NC}"
    echo -e "\n${YELLOW}Database logs:${NC}"
    docker-compose logs --tail=20 postgres
fi

# Test API endpoints
echo -e "\n${YELLOW}9. Testing API endpoints...${NC}"

# Test products endpoint
if curl -f -s http://localhost:8080/api/products > /dev/null; then
    echo -e "${GREEN}✓ Products API is working${NC}"
else
    echo -e "${RED}❌ Products API failed${NC}"
fi

# Test users endpoint
if curl -f -s http://localhost:8080/api/users > /dev/null; then
    echo -e "${GREEN}✓ Users API is working${NC}"
else
    echo -e "${RED}❌ Users API failed${NC}"
fi

# Check for common errors in logs
echo -e "\n${YELLOW}10. Checking for common errors...${NC}"

# Check for logback errors
if docker-compose logs | grep -q "No encoder set for the appender"; then
    echo -e "${RED}❌ Found logback encoder error${NC}"
    echo "This has been fixed in the latest configuration."
else
    echo -e "${GREEN}✓ No logback encoder errors found${NC}"
fi

# Check for database connection errors
if docker-compose logs | grep -q "Connection refused"; then
    echo -e "${RED}❌ Found database connection errors${NC}"
else
    echo -e "${GREEN}✓ No database connection errors found${NC}"
fi

# Check for Spring Boot startup errors
if docker-compose logs backend | grep -q "Application startup failed"; then
    echo -e "${RED}❌ Found Spring Boot startup errors${NC}"
else
    echo -e "${GREEN}✓ No Spring Boot startup errors found${NC}"
fi

echo -e "\n${YELLOW}📊 Startup Test Summary${NC}"
echo "========================"

# Count running services
RUNNING_SERVICES=$(docker-compose ps --filter "status=running" --format "table {{.Name}}" | wc -l)
RUNNING_SERVICES=$((RUNNING_SERVICES - 1)) # Subtract header line

echo "Running services: $RUNNING_SERVICES"

if [ $RUNNING_SERVICES -ge 5 ]; then
    echo -e "${GREEN}🎉 Application startup successful!${NC}"
    echo -e "\n${YELLOW}Access URLs:${NC}"
    echo "Frontend: http://localhost:3000"
    echo "Backend API: http://localhost:8080"
    echo "Health Check: http://localhost:8080/actuator/health"
    exit 0
else
    echo -e "${RED}❌ Application startup failed${NC}"
    echo -e "\n${YELLOW}Debugging tips:${NC}"
    echo "1. Check logs: docker-compose logs"
    echo "2. Restart services: docker-compose restart"
    echo "3. Rebuild: docker-compose build --no-cache"
    exit 1
fi 