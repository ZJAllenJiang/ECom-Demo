#!/bin/bash

echo "🚀 Starting Local E-Commerce Platform Deployment..."

# Check if Docker and Docker Compose are installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Check if .env file exists, if not create from example
if [ ! -f .env ]; then
    echo "📝 Creating .env file from env.example..."
    cp env.example .env
    echo "⚠️  Please update .env file with your actual configuration values"
    echo "   - Update STRIPE_SECRET_KEY with your test key"
    echo "   - Update STRIPE_PUBLISHABLE_KEY with your test key"
    read -p "Press Enter after updating .env file..."
fi

# Build and start services
echo "🔨 Building and starting services..."
docker-compose down
docker-compose build --no-cache
docker-compose up -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# Check service health
echo "🏥 Checking service health..."
docker-compose ps

# Test backend health
echo "🔍 Testing backend health..."
curl -f http://localhost:8080/actuator/health || echo "❌ Backend health check failed"

# Test frontend
echo "🔍 Testing frontend..."
curl -f http://localhost:3000 || echo "❌ Frontend health check failed"

echo "✅ Deployment completed!"
echo ""
echo "📋 Access URLs:"
echo "   Frontend: http://localhost:3000"
echo "   Backend API: http://localhost:8080"
echo "   ActiveMQ Console: http://localhost:8161 (admin/admin)"
echo "   Kibana: http://localhost:5601"
echo "   Elasticsearch: http://localhost:9200"
echo ""
echo "📝 Useful commands:"
echo "   View logs: docker-compose logs -f"
echo "   Stop services: docker-compose down"
echo "   Restart services: docker-compose restart" 