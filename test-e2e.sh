#!/bin/bash

echo "🧪 Starting End-to-End Tests..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
PASSED=0
FAILED=0

# Function to run a test
run_test() {
    local test_name="$1"
    local test_command="$2"
    
    echo -n "Testing: $test_name... "
    
    if eval "$test_command" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ PASSED${NC}"
        ((PASSED++))
    else
        echo -e "${RED}✗ FAILED${NC}"
        ((FAILED++))
    fi
}

# Function to check if service is running
check_service() {
    local service_name="$1"
    local url="$2"
    
    if curl -f -s "$url" > /dev/null; then
        return 0
    else
        return 1
    fi
}

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 10

# Test 1: Check if all services are running
echo ""
echo "📋 Service Health Checks:"

run_test "Backend API Health" "check_service 'Backend' 'http://localhost:8080/actuator/health'"
run_test "Frontend Application" "check_service 'Frontend' 'http://localhost:3000'"
run_test "PostgreSQL Database" "docker-compose exec -T postgres pg_isready -U postgres"
run_test "ActiveMQ Broker" "check_service 'ActiveMQ' 'http://localhost:8161'"
run_test "Elasticsearch" "check_service 'Elasticsearch' 'http://localhost:9200'"
run_test "Kibana" "check_service 'Kibana' 'http://localhost:5601'"

# Test 2: API Endpoint Tests
echo ""
echo "🔌 API Endpoint Tests:"

# Test Products API
run_test "Get Products" "curl -f -s 'http://localhost:8080/api/products' | jq -e 'length > 0'"
run_test "Get Product by ID" "curl -f -s 'http://localhost:8080/api/products/1' | jq -e '.id == 1'"

# Test Users API
run_test "Get Users" "curl -f -s 'http://localhost:8080/api/users' | jq -e 'length > 0'"
run_test "Check Username Exists" "curl -f -s 'http://localhost:8080/api/users/check-username/admin' | grep -q 'true'"

# Test User Registration with Validation
echo ""
echo "👤 User Registration Tests:"

# Test valid user registration
VALID_USER='{
  "username": "testuser123",
  "email": "test123@example.com",
  "password": "Password123",
  "firstName": "Test",
  "lastName": "User"
}'

run_test "Valid User Registration" "curl -f -s -X POST 'http://localhost:8080/api/users' \
  -H 'Content-Type: application/json' \
  -d '$VALID_USER' | jq -e '.id'"

# Test invalid user registration (short password)
INVALID_USER='{
  "username": "testuser456",
  "email": "test456@example.com",
  "password": "123",
  "firstName": "Test",
  "lastName": "User"
}'

run_test "Invalid User Registration (Short Password)" "curl -s -X POST 'http://localhost:8080/api/users' \
  -H 'Content-Type: application/json' \
  -d '$INVALID_USER' | jq -e '.error == \"Validation failed\"'"

# Test invalid email
INVALID_EMAIL_USER='{
  "username": "testuser789",
  "email": "invalid-email",
  "password": "Password123",
  "firstName": "Test",
  "lastName": "User"
}'

run_test "Invalid User Registration (Invalid Email)" "curl -s -X POST 'http://localhost:8080/api/users' \
  -H 'Content-Type: application/json' \
  -d '$INVALID_EMAIL_USER' | jq -e '.error == \"Validation failed\"'"

# Test 3: Database Tests
echo ""
echo "🗄️ Database Tests:"

run_test "Database Connection" "docker-compose exec -T postgres psql -U postgres -d ecommerce -c 'SELECT COUNT(*) FROM users;'"
run_test "Products Table" "docker-compose exec -T postgres psql -U postgres -d ecommerce -c 'SELECT COUNT(*) FROM products;'"
run_test "Orders Table" "docker-compose exec -T postgres psql -U postgres -d ecommerce -c 'SELECT COUNT(*) FROM orders;'"

# Test 4: Frontend Tests
echo ""
echo "🌐 Frontend Tests:"

# Check if React app loads
run_test "React App Loads" "curl -f -s 'http://localhost:3000' | grep -q 'E-Commerce Platform'"

# Test 5: Security Tests
echo ""
echo "🔒 Security Tests:"

# Test CORS headers
run_test "CORS Headers Present" "curl -f -s -I 'http://localhost:8080/api/products' | grep -q 'Access-Control-Allow-Origin'"

# Test authentication endpoints (should be accessible for demo)
run_test "Public Endpoints Accessible" "curl -f -s 'http://localhost:8080/api/products'"

# Test 6: Error Handling Tests
echo ""
echo "⚠️ Error Handling Tests:"

# Test 404 endpoint
run_test "404 Error Handling" "curl -s 'http://localhost:8080/api/nonexistent' | jq -e '.error'"

# Test invalid product ID
run_test "Invalid Product ID" "curl -s 'http://localhost:8080/api/products/999999' | jq -e '.error'"

# Test 7: Performance Tests
echo ""
echo "⚡ Performance Tests:"

# Test API response time
START_TIME=$(date +%s%N)
curl -f -s 'http://localhost:8080/api/products' > /dev/null
END_TIME=$(date +%s%N)
RESPONSE_TIME=$(( (END_TIME - START_TIME) / 1000000 ))

if [ $RESPONSE_TIME -lt 1000 ]; then
    echo -e "API Response Time: ${GREEN}${RESPONSE_TIME}ms ✓ PASSED${NC}"
    ((PASSED++))
else
    echo -e "API Response Time: ${RED}${RESPONSE_TIME}ms ✗ FAILED (too slow)${NC}"
    ((FAILED++))
fi

# Test 8: Docker Container Health
echo ""
echo "🐳 Docker Container Tests:"

run_test "All Containers Running" "docker-compose ps | grep -q 'Up'"
run_test "No Container Errors" "docker-compose logs --tail=50 | grep -q 'ERROR' && false || true"

# Test 9: Environment Configuration
echo ""
echo "⚙️ Environment Tests:"

run_test "Environment File Exists" "[ -f .env ]"
run_test "Docker Compose File Valid" "docker-compose config > /dev/null"

# Summary
echo ""
echo "📊 Test Summary:"
echo "=================="
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo "Total: $((PASSED + FAILED))"

if [ $FAILED -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 All tests passed! The application is working correctly.${NC}"
    exit 0
else
    echo ""
    echo -e "${RED}❌ Some tests failed. Please check the logs and fix the issues.${NC}"
    echo ""
    echo "🔍 Debugging Tips:"
    echo "1. Check service logs: docker-compose logs"
    echo "2. Verify environment variables in .env file"
    echo "3. Ensure all services are running: docker-compose ps"
    echo "4. Check database connectivity"
    exit 1
fi 