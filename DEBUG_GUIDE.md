# E-Commerce Platform Debug & Verification Guide

## 🚀 Quick Start

### 1. Initial Setup
```bash
# Clone and navigate to project
cd ECom-Demo

# Make scripts executable
chmod +x deploy-local.sh test-e2e.sh run-tests.sh

# Create environment file
cp env.example .env

# Edit .env with your configuration
nano .env
```

### 2. Deploy Application
```bash
# Deploy all services
./deploy-local.sh

# Or manually
docker-compose up -d
```

### 3. Run Tests
```bash
# Run all tests
./run-tests.sh

# Run end-to-end tests only
./test-e2e.sh

# Run specific test suites
cd backend && mvn test
cd frontend && npm test
```

## 🔍 Verification Checklist

### ✅ Pre-Deployment Checks
- [ ] Docker and Docker Compose installed
- [ ] Java 11+ installed (for local development)
- [ ] Node.js 16+ installed (for local development)
- [ ] Environment file (.env) configured
- [ ] Stripe test keys configured (for payment testing)

### ✅ Service Health Checks
```bash
# Check all services
docker-compose ps

# Check individual services
curl http://localhost:8080/actuator/health  # Backend
curl http://localhost:3000                  # Frontend
curl http://localhost:8161                  # ActiveMQ
curl http://localhost:9200                  # Elasticsearch
curl http://localhost:5601                  # Kibana
```

### ✅ Database Verification
```bash
# Connect to database
docker-compose exec postgres psql -U postgres -d ecommerce

# Check tables
\dt

# Check sample data
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM products;
SELECT COUNT(*) FROM orders;
```

## 🧪 Test Categories

### 1. Unit Tests
**Backend Unit Tests:**
- UserService password hashing
- OrderService business logic validation
- Input validation DTOs
- Exception handling

**Frontend Unit Tests:**
- OrderHistory component functionality
- API service error handling
- Error boundary behavior

### 2. Integration Tests
**Backend Integration Tests:**
- UserController API endpoints
- Validation error responses
- Global exception handler
- Security configuration

**Frontend Integration Tests:**
- API service integration
- Component state management
- Error handling flows

### 3. End-to-End Tests
**Service Integration:**
- Database connectivity
- API endpoint availability
- Frontend-backend communication
- Payment flow simulation

**Performance Tests:**
- API response times
- Database query performance
- Frontend load times

## 🐛 Common Issues & Solutions

### Issue 1: Services Not Starting
**Symptoms:**
- `docker-compose ps` shows services as "Exit" or "Restarting"
- Port conflicts in logs

**Solutions:**
```bash
# Check for port conflicts
lsof -i :8080
lsof -i :3000
lsof -i :5432

# Stop conflicting services
sudo systemctl stop postgresql  # If using system PostgreSQL
sudo systemctl stop nginx       # If using system nginx

# Restart Docker services
docker-compose down
docker-compose up -d
```

### Issue 2: Database Connection Errors
**Symptoms:**
- Backend fails to start
- "Connection refused" errors in logs

**Solutions:**
```bash
# Check database container
docker-compose logs postgres

# Restart database
docker-compose restart postgres

# Wait for database to be ready
docker-compose exec postgres pg_isready -U postgres

# Check database schema
docker-compose exec postgres psql -U postgres -d ecommerce -c "\dt"
```

### Issue 3: Frontend Build Failures
**Symptoms:**
- Frontend container fails to build
- Node modules issues

**Solutions:**
```bash
# Clean and rebuild frontend
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run build

# Rebuild Docker container
docker-compose build --no-cache frontend
docker-compose up -d frontend
```

### Issue 4: API Validation Errors
**Symptoms:**
- User registration fails
- "Validation failed" errors

**Solutions:**
```bash
# Check validation DTOs
# Verify UserRegistrationDTO annotations

# Test API manually
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Issue 5: Security Configuration Issues
**Symptoms:**
- CORS errors in browser
- Authentication failures

**Solutions:**
```bash
# Check security configuration
# Verify SecurityConfig.java CORS settings

# Test CORS headers
curl -I http://localhost:8080/api/products

# Check authentication endpoints
curl http://localhost:8080/api/products  # Should be accessible
```

## 📊 Monitoring & Logs

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres

# Real-time logs
docker-compose logs -f --tail=100
```

### Performance Monitoring
```bash
# Check resource usage
docker stats

# Monitor API response times
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8080/api/products

# Database performance
docker-compose exec postgres psql -U postgres -d ecommerce -c "SELECT * FROM pg_stat_activity;"
```

### Health Checks
```bash
# Backend health
curl http://localhost:8080/actuator/health

# Database health
docker-compose exec postgres pg_isready -U postgres

# Service status
docker-compose ps
```

## 🔧 Development Tools

### Database Management
```bash
# Connect to database
docker-compose exec postgres psql -U postgres -d ecommerce

# Useful commands:
\dt                    # List tables
\d users              # Describe table
SELECT * FROM users;   # Query data
\q                    # Quit
```

### API Testing
```bash
# Test products API
curl http://localhost:8080/api/products

# Test user registration
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"Password123","firstName":"Test","lastName":"User"}'

# Test validation
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"","email":"invalid","password":"123"}'
```

### Frontend Development
```bash
# Start frontend in development mode
cd frontend
npm start

# Run tests
npm test

# Build for production
npm run build
```

## 🚨 Emergency Procedures

### Complete Reset
```bash
# Stop all services
docker-compose down

# Remove all containers and volumes
docker-compose down -v
docker system prune -a

# Rebuild everything
docker-compose build --no-cache
docker-compose up -d
```

### Database Reset
```bash
# Drop and recreate database
docker-compose exec postgres psql -U postgres -c "DROP DATABASE ecommerce;"
docker-compose exec postgres psql -U postgres -c "CREATE DATABASE ecommerce;"

# Re-run schema
docker-compose exec postgres psql -U postgres -d ecommerce -f /docker-entrypoint-initdb.d/schema.sql
```

### Environment Reset
```bash
# Remove environment file
rm .env

# Recreate from example
cp env.example .env

# Edit with your values
nano .env
```

## 📈 Performance Optimization

### Database Optimization
```sql
-- Add indexes for better performance
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_products_name ON products(name);
```

### Application Optimization
```bash
# Increase JVM heap size
export JAVA_OPTS="-Xmx2g -Xms1g"

# Enable JVM monitoring
export JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote"
```

### Docker Optimization
```bash
# Use Docker build cache
docker-compose build --parallel

# Limit resource usage
docker-compose up -d --scale backend=1 --scale frontend=1
```

## 📞 Support

### Log Files Location
- Backend logs: `logs/ecommerce.log`
- Docker logs: `docker-compose logs`
- Application logs: `docker-compose logs backend`

### Useful Commands
```bash
# Quick health check
./test-e2e.sh

# Full test suite
./run-tests.sh

# Service status
docker-compose ps

# Resource usage
docker stats
```

### Debug Mode
```bash
# Enable debug logging
export LOG_LEVEL=DEBUG

# Restart services
docker-compose restart backend
```

This guide should help you verify, debug, and maintain the e-commerce platform effectively. 