-- Create database (run this separately if needed)
-- CREATE DATABASE ecommerce;

-- Use the database
-- \c ecommerce;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INTEGER DEFAULT 0,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    stripe_payment_intent_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create order_items table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id),
    quantity INTEGER NOT NULL,
    price DECIMAL(10,2) NOT NULL
);

-- Insert sample users
INSERT INTO users (username, email, password, first_name, last_name) VALUES
('john_doe', 'john@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Doe'),
('jane_smith', 'jane@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Jane', 'Smith'),
('admin', 'admin@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin', 'User')
ON CONFLICT (username) DO NOTHING;

-- Insert sample products
INSERT INTO products (name, description, price, stock, image_url) VALUES
('MacBook Pro 16"', 'High-performance laptop for professionals and creatives', 2499.99, 25, 'https://via.placeholder.com/400x300?text=MacBook+Pro'),
('iPhone 14 Pro', 'Latest iPhone with advanced camera system', 999.99, 50, 'https://via.placeholder.com/400x300?text=iPhone+14+Pro'),
('AirPods Pro', 'Premium noise-cancelling wireless earbuds', 249.99, 100, 'https://via.placeholder.com/400x300?text=AirPods+Pro'),
('iPad Air', '10.9-inch iPad with M1 chip', 599.99, 40, 'https://via.placeholder.com/400x300?text=iPad+Air'),
('Apple Watch Series 8', 'Advanced health and fitness tracking', 399.99, 60, 'https://via.placeholder.com/400x300?text=Apple+Watch'),
('Samsung Galaxy S23', 'Flagship Android smartphone', 799.99, 35, 'https://via.placeholder.com/400x300?text=Galaxy+S23'),
('Dell XPS 13', 'Ultra-portable laptop with stunning display', 1199.99, 20, 'https://via.placeholder.com/400x300?text=Dell+XPS+13'),
('Sony WH-1000XM5', 'Industry-leading noise canceling headphones', 399.99, 45, 'https://via.placeholder.com/400x300?text=Sony+Headphones'),
('Nintendo Switch', 'Hybrid gaming console', 299.99, 30, 'https://via.placeholder.com/400x300?text=Nintendo+Switch'),
('Google Pixel 7', 'AI-powered Android phone', 599.99, 25, 'https://via.placeholder.com/400x300?text=Pixel+7')
ON CONFLICT (name) DO NOTHING;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);