# E-Commerce Platform

A full-stack e-commerce application built with Spring Boot, React, PostgreSQL, ActiveMQ, Elasticsearch, and Stripe integration.

## Features

- **Product Management**: Browse, search, and manage products
- **Shopping Cart**: Add/remove items, update quantities
- **Order Processing**: Complete order workflow with ActiveMQ messaging
- **Payment Integration**: Secure payments with Stripe
- **Search & Analytics**: Elasticsearch integration for product search
- **Monitoring**: ELK stack for logging and monitoring
- **Containerized**: Docker and Docker Compose setup

## Technology Stack

### Backend
- Spring Boot 2.7.x
- Java 11
- PostgreSQL
- ActiveMQ
- Elasticsearch
- Stripe API

### Frontend
- React 18
- Stripe React Elements
- Responsive CSS

### DevOps
- Docker & Docker Compose
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Nginx

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Java 11+ (for local development)
- Node.js 16+ (for local development)
- Stripe Account (for payment processing)

### 1. Clone and Setup
```bash
git clone <repository-url>
cd ecommerce-platform
```

### 2. Configure Environment
```bash
# Copy environment file
cp .env.example .env

# Edit .env file with your Stripe keys
nano .env
```

### 3. Run with Docker Compose
```bash
# Start all services
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f backend
```

### 4. Access Applications
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **ActiveMQ Console**: http://localhost:8161 (admin/admin)
- **Kibana**: http://localhost:5601
- **Elasticsearch**: http://localhost:9200

### 5. Initialize Database
```bash
# Connect to PostgreSQL container
docker-compose exec postgres psql -U postgres -d ecommerce

# Run schema.sql if needed
\i schema.sql
```

## Local Development

### Backend Development
```bash
cd backend

# Install dependencies
mvn clean install

# Run application
mvn spring-boot:run
```

### Frontend Development
```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

## API Endpoints

### Products
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?q={query}` - Search products
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Orders
- `POST /api/orders` - Create order
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/user/{userId}` - Get user orders
- `PUT /api/orders/{id}/status` - Update order status

### Payments
- `POST /api/payments/create-payment-intent` - Create Stripe payment intent
- `POST /api/payments/confirm-payment` - Confirm payment

## Testing

### Run Backend Tests
```bash
cd backend
mvn test
```

### Run Frontend Tests
```bash
cd frontend
npm test
```

## Monitoring and Logging

### View Application Logs
```bash
# Backend logs
docker-compose logs -f backend

# All services logs
docker-compose logs -f
```

### Access Kibana Dashboard
1. Open http://localhost:5601
2. Create index pattern: `ecommerce-logs-*`
3. Explore logs and create visualizations

## Production Deployment

### AWS Deployment
1. Set up AWS RDS for PostgreSQL
2. Set up AWS MQ for ActiveMQ
3. Use AWS Elasticsearch Service
4. Deploy containers using AWS ECS or EKS
5. Set up Application Load Balancer
6. Configure environment variables

### Environment Variables for Production
```bash
# Update .env for production
DB_HOST=your-rds-endpoint
ACTIVEMQ_BROKER_URL=ssl://your-aws-mq-endpoint:61617
ELASTICSEARCH_HOST=your-es-endpoint
STRIPE_SECRET_KEY=sk_live_your_live_secret_key
```

## Security Considerations

- Use HTTPS in production
- Implement user authentication/authorization
- Validate all inputs
- Use secure database connections
- Implement rate limiting
- Regular security updates

## Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## License

MIT License - see LICENSE file for details