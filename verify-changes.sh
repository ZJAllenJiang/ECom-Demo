#!/bin/bash

echo "🔍 Verifying E-Commerce Platform Changes"
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counter
CHECKS_PASSED=0
CHECKS_FAILED=0

# Function to check file existence
check_file() {
    local file_path="$1"
    local description="$2"
    
    if [ -f "$file_path" ]; then
        echo -e "${GREEN}✓ $description${NC}"
        ((CHECKS_PASSED++))
    else
        echo -e "${RED}✗ $description (missing: $file_path)${NC}"
        ((CHECKS_FAILED++))
    fi
}

# Function to check directory existence
check_directory() {
    local dir_path="$1"
    local description="$2"
    
    if [ -d "$dir_path" ]; then
        echo -e "${GREEN}✓ $description${NC}"
        ((CHECKS_PASSED++))
    else
        echo -e "${RED}✗ $description (missing: $dir_path)${NC}"
        ((CHECKS_FAILED++))
    fi
}

# Function to check file content
check_content() {
    local file_path="$1"
    local search_term="$2"
    local description="$3"
    
    if grep -q "$search_term" "$file_path" 2>/dev/null; then
        echo -e "${GREEN}✓ $description${NC}"
        ((CHECKS_PASSED++))
    else
        echo -e "${RED}✗ $description (missing: $search_term in $file_path)${NC}"
        ((CHECKS_FAILED++))
    fi
}

echo -e "\n${BLUE}1. Critical Fixes Verification${NC}"
echo "--------------------------------"

# Check missing OrderHistory component
check_file "frontend/src/components/OrderHistory.js" "OrderHistory component created"
check_file "frontend/src/components/OrderHistory.css" "OrderHistory CSS styles created"

# Check JPA mapping fix
check_content "backend/src/main/java/org/allen/entity/Order.java" "@JoinColumn" "JPA mapping fixed in Order entity"

# Check environment configuration
check_file "env.example" "Environment example file created"
check_content ".gitignore" ".env" "Environment files added to .gitignore"

echo -e "\n${BLUE}2. Security & Validation Fixes${NC}"
echo "-----------------------------------"

# Check validation DTOs
check_file "backend/src/main/java/org/allen/dto/UserRegistrationDTO.java" "UserRegistrationDTO with validation created"

# Check validation dependency
check_content "backend/pom.xml" "spring-boot-starter-validation" "Validation dependency added"

# Check password service
check_file "backend/src/main/java/org/allen/service/PasswordService.java" "PasswordService for hashing created"

# Check security configuration
check_file "backend/src/main/java/org/allen/config/SecurityConfig.java" "Security configuration created"
check_content "backend/pom.xml" "spring-boot-starter-security" "Security dependency added"

echo -e "\n${BLUE}3. Error Handling Fixes${NC}"
echo "----------------------------"

# Check exception classes
check_file "backend/src/main/java/org/allen/exception/GlobalExceptionHandler.java" "Global exception handler created"
check_file "backend/src/main/java/org/allen/exception/ResourceNotFoundException.java" "ResourceNotFoundException created"
check_file "backend/src/main/java/org/allen/exception/BusinessException.java" "BusinessException created"

# Check frontend error handling
check_file "frontend/src/components/ErrorBoundary.js" "React ErrorBoundary component created"
check_content "frontend/src/services/api.js" "ApiError" "API error handling enhanced"

echo -e "\n${BLUE}4. Business Logic Fixes${NC}"
echo "-----------------------------"

# Check OrderService validation
check_content "backend/src/main/java/org/allen/service/OrderService.java" "BusinessException" "Business logic validation added to OrderService"

# Check UserService password hashing
check_content "backend/src/main/java/org/allen/service/UserService.java" "passwordService.hashPassword" "Password hashing added to UserService"

echo -e "\n${BLUE}5. Infrastructure Fixes${NC}"
echo "----------------------------"

# Check Docker health checks
check_content "docker-compose.yml" "healthcheck" "Docker health checks added"

# Check database constraints
check_content "backend/src/main/resources/schema.sql" "CHECK" "Database constraints added"

echo -e "\n${BLUE}6. Test Files Verification${NC}"
echo "-------------------------------"

# Check backend tests
check_file "backend/src/test/java/org/allen/service/UserServiceTest.java" "UserService unit tests created"
check_file "backend/src/test/java/org/allen/service/OrderServiceTest.java" "OrderService unit tests created"
check_file "backend/src/test/java/org/allen/service/PasswordServiceTest.java" "PasswordService unit tests created"
check_file "backend/src/test/java/org/allen/controller/UserControllerIntegrationTest.java" "UserController integration tests created"

# Check frontend tests
check_file "frontend/src/components/__tests__/OrderHistory.test.js" "OrderHistory component tests created"
check_file "frontend/src/services/__tests__/api.test.js" "API service tests created"

echo -e "\n${BLUE}7. Deployment & Testing Scripts${NC}"
echo "-------------------------------------"

# Check deployment scripts
check_file "deploy-local.sh" "Local deployment script created"
check_file "test-e2e.sh" "End-to-end test script created"
check_file "run-tests.sh" "Test runner script created"
check_file "DEBUG_GUIDE.md" "Debug and verification guide created"

# Make scripts executable
chmod +x deploy-local.sh test-e2e.sh run-tests.sh verify-changes.sh

echo -e "\n${BLUE}8. Configuration Files${NC}"
echo "---------------------------"

# Check updated configuration files
check_content "frontend/src/App.js" "ErrorBoundary" "ErrorBoundary integrated into App.js"
check_content "frontend/src/App.css" "error-boundary" "Error boundary styles added"

echo -e "\n${BLUE}9. Dependencies Verification${NC}"
echo "--------------------------------"

# Check if all required dependencies are present
check_content "backend/pom.xml" "spring-boot-starter-validation" "Validation starter"
check_content "backend/pom.xml" "spring-boot-starter-security" "Security starter"
check_content "frontend/package.json" "@testing-library" "Testing library"

echo -e "\n${BLUE}10. File Structure Verification${NC}"
echo "-----------------------------------"

# Check directory structure
check_directory "backend/src/main/java/org/allen/exception" "Exception package created"
check_directory "backend/src/main/java/org/allen/dto" "DTO package created"
check_directory "frontend/src/components/__tests__" "Frontend test directory created"
check_directory "frontend/src/services/__tests__" "API test directory created"

# Summary
echo -e "\n${BLUE}📊 Verification Summary${NC}"
echo "========================"
echo -e "${GREEN}Passed: $CHECKS_PASSED${NC}"
echo -e "${RED}Failed: $CHECKS_FAILED${NC}"
echo "Total: $((CHECKS_PASSED + CHECKS_FAILED))"

if [ $CHECKS_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 All changes verified successfully!${NC}"
    echo -e "\n${YELLOW}Next Steps:${NC}"
    echo "1. Run: ./deploy-local.sh"
    echo "2. Run: ./test-e2e.sh"
    echo "3. Run: ./run-tests.sh"
    echo "4. Access: http://localhost:3000"
    exit 0
else
    echo -e "\n${RED}❌ Some changes are missing. Please review the failed checks above.${NC}"
    exit 1
fi 