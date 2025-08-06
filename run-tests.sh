#!/bin/bash

echo "🧪 E-Commerce Platform Test Suite"
echo "=================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to run test suite
run_test_suite() {
    local suite_name="$1"
    local test_command="$2"
    
    echo -e "\n${BLUE}Running $suite_name...${NC}"
    echo "----------------------------------------"
    
    if eval "$test_command"; then
        echo -e "${GREEN}✓ $suite_name completed successfully${NC}"
        ((PASSED_TESTS++))
    else
        echo -e "${RED}✗ $suite_name failed${NC}"
        ((FAILED_TESTS++))
    fi
    
    ((TOTAL_TESTS++))
}

# Check if services are running
check_services() {
    echo -e "\n${YELLOW}Checking if services are running...${NC}"
    
    if ! docker-compose ps | grep -q "Up"; then
        echo -e "${RED}❌ Services are not running. Please start them first with:${NC}"
        echo "   ./deploy-local.sh"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Services are running${NC}"
}

# 1. Backend Unit Tests
run_backend_unit_tests() {
    echo "Running backend unit tests..."
    cd backend
    
    # Run Maven tests
    if mvn test -q; then
        echo "Backend unit tests passed"
        return 0
    else
        echo "Backend unit tests failed"
        return 1
    fi
}

# 2. Backend Integration Tests
run_backend_integration_tests() {
    echo "Running backend integration tests..."
    cd backend
    
    # Run integration tests
    if mvn test -Dtest=*IntegrationTest -q; then
        echo "Backend integration tests passed"
        return 0
    else
        echo "Backend integration tests failed"
        return 1
    fi
}

# 3. Frontend Unit Tests
run_frontend_unit_tests() {
    echo "Running frontend unit tests..."
    cd frontend
    
    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
        echo "Installing frontend dependencies..."
        npm install --silent
    fi
    
    # Run tests
    if npm test -- --watchAll=false --passWithNoTests --silent; then
        echo "Frontend unit tests passed"
        return 0
    else
        echo "Frontend unit tests failed"
        return 1
    fi
}

# 4. Frontend Integration Tests
run_frontend_integration_tests() {
    echo "Running frontend integration tests..."
    cd frontend
    
    # Run integration tests
    if npm test -- --testPathPattern="integration" --watchAll=false --passWithNoTests --silent; then
        echo "Frontend integration tests passed"
        return 0
    else
        echo "Frontend integration tests failed"
        return 1
    fi
}

# 5. API Tests
run_api_tests() {
    echo "Running API tests..."
    
    # Test basic API endpoints
    local api_tests=(
        "curl -f -s 'http://localhost:8080/api/products' > /dev/null"
        "curl -f -s 'http://localhost:8080/api/users' > /dev/null"
        "curl -f -s 'http://localhost:8080/actuator/health' > /dev/null"
    )
    
    local passed=0
    local total=${#api_tests[@]}
    
    for test in "${api_tests[@]}"; do
        if eval "$test"; then
            ((passed++))
        fi
    done
    
    if [ $passed -eq $total ]; then
        echo "API tests passed"
        return 0
    else
        echo "API tests failed ($passed/$total)"
        return 1
    fi
}

# 6. Database Tests
run_database_tests() {
    echo "Running database tests..."
    
    # Test database connectivity and basic operations
    local db_tests=(
        "docker-compose exec -T postgres pg_isready -U postgres"
        "docker-compose exec -T postgres psql -U postgres -d ecommerce -c 'SELECT 1;' > /dev/null"
        "docker-compose exec -T postgres psql -U postgres -d ecommerce -c 'SELECT COUNT(*) FROM users;' > /dev/null"
    )
    
    local passed=0
    local total=${#db_tests[@]}
    
    for test in "${db_tests[@]}"; do
        if eval "$test"; then
            ((passed++))
        fi
    done
    
    if [ $passed -eq $total ]; then
        echo "Database tests passed"
        return 0
    else
        echo "Database tests failed ($passed/$total)"
        return 1
    fi
}

# 7. Security Tests
run_security_tests() {
    echo "Running security tests..."
    
    # Test password hashing
    cd backend
    if mvn test -Dtest=PasswordServiceTest -q; then
        echo "Password security tests passed"
        return 0
    else
        echo "Password security tests failed"
        return 1
    fi
}

# 8. Performance Tests
run_performance_tests() {
    echo "Running performance tests..."
    
    # Test API response time
    local start_time=$(date +%s%N)
    curl -f -s 'http://localhost:8080/api/products' > /dev/null
    local end_time=$(date +%s%N)
    local response_time=$(( (end_time - start_time) / 1000000 ))
    
    if [ $response_time -lt 1000 ]; then
        echo "Performance test passed (${response_time}ms)"
        return 0
    else
        echo "Performance test failed (${response_time}ms - too slow)"
        return 1
    fi
}

# Main test execution
main() {
    # Check if we're in the right directory
    if [ ! -f "docker-compose.yml" ]; then
        echo -e "${RED}❌ Please run this script from the project root directory${NC}"
        exit 1
    fi
    
    # Check services
    check_services
    
    echo -e "\n${YELLOW}Starting test execution...${NC}"
    
    # Run all test suites
    run_test_suite "Backend Unit Tests" "run_backend_unit_tests"
    run_test_suite "Backend Integration Tests" "run_backend_integration_tests"
    run_test_suite "Frontend Unit Tests" "run_frontend_unit_tests"
    run_test_suite "Frontend Integration Tests" "run_frontend_integration_tests"
    run_test_suite "API Tests" "run_api_tests"
    run_test_suite "Database Tests" "run_database_tests"
    run_test_suite "Security Tests" "run_security_tests"
    run_test_suite "Performance Tests" "run_performance_tests"
    
    # Summary
    echo -e "\n${BLUE}Test Summary${NC}"
    echo "============"
    echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
    echo -e "${RED}Failed: $FAILED_TESTS${NC}"
    echo "Total: $TOTAL_TESTS"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "\n${GREEN}🎉 All test suites passed!${NC}"
        exit 0
    else
        echo -e "\n${RED}❌ Some test suites failed.${NC}"
        echo -e "\n${YELLOW}Debugging Tips:${NC}"
        echo "1. Check service logs: docker-compose logs"
        echo "2. Verify environment configuration"
        echo "3. Ensure all dependencies are installed"
        echo "4. Check database connectivity"
        exit 1
    fi
}

# Run main function
main 