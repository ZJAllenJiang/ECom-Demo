import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import ProductList from './components/ProductList';
import ProductDetail from './components/ProductDetail';
import Cart from './components/Cart';
import Checkout from './components/Checkout';
import OrderHistory from './components/OrderHistory';
import ErrorBoundary from './components/ErrorBoundary';
import { api } from './services/api';
import './App.css';

function App() {
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchProducts();
    loadCartFromStorage();
  }, []);

  useEffect(() => {
    saveCartToStorage();
  }, [cart]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const data = await api.getProducts();
      setProducts(data);
    } catch (error) {
      console.error('Error fetching products:', error);
      setError('Failed to load products');
    } finally {
      setLoading(false);
    }
  };

  const loadCartFromStorage = () => {
    try {
      const savedCart = localStorage.getItem('cart');
      if (savedCart) {
        setCart(JSON.parse(savedCart));
      }
    } catch (error) {
      console.error('Error loading cart from storage:', error);
    }
  };

  const saveCartToStorage = () => {
    try {
      localStorage.setItem('cart', JSON.stringify(cart));
    } catch (error) {
      console.error('Error saving cart to storage:', error);
    }
  };

  const addToCart = (product, quantity = 1) => {
    setCart(prevCart => {
      const existingItem = prevCart.find(item => item.id === product.id);
      if (existingItem) {
        return prevCart.map(item =>
          item.id === product.id
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
      } else {
        return [...prevCart, { ...product, quantity }];
      }
    });
  };

  const removeFromCart = (productId) => {
    setCart(prevCart => prevCart.filter(item => item.id !== productId));
  };

  const updateCartQuantity = (productId, quantity) => {
    if (quantity <= 0) {
      removeFromCart(productId);
    } else {
      setCart(prevCart =>
        prevCart.map(item =>
          item.id === productId
            ? { ...item, quantity }
            : item
        )
      );
    }
  };

  const clearCart = () => {
    setCart([]);
  };

  const getTotalPrice = () => {
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0).toFixed(2);
  };

  const getCartItemCount = () => {
    return cart.reduce((total, item) => total + item.quantity, 0);
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Loading products...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <h2>Error</h2>
        <p>{error}</p>
        <button onClick={fetchProducts} className="retry-button">
          Retry
        </button>
      </div>
    );
  }

  return (
    <ErrorBoundary>
      <Router>
        <div className="App">
          <header className="App-header">
            <nav className="main-nav">
              <div className="nav-brand">
                <Link to="/" className="brand-link">
                  <h1>E-Commerce Platform</h1>
                </Link>
              </div>

              <div className="nav-links">
                <Link to="/" className="nav-link">Products</Link>
                <Link to="/orders" className="nav-link">Orders</Link>
                <Link to="/cart" className="nav-link cart-link">
                  Cart ({getCartItemCount()})
                  {getCartItemCount() > 0 && (
                    <span className="cart-badge">{getCartItemCount()}</span>
                  )}
                </Link>
              </div>
            </nav>
          </header>

          <main className="main-content">
            <Routes>
              <Route
                path="/"
                element={
                  <ProductList
                    products={products}
                    addToCart={addToCart}
                    onRefresh={fetchProducts}
                  />
                }
              />

              <Route
                path="/product/:id"
                element={
                  <ProductDetail
                    products={products}
                    addToCart={addToCart}
                  />
                }
              />

              <Route
                path="/cart"
                element={
                  <Cart
                    cart={cart}
                    updateQuantity={updateCartQuantity}
                    removeItem={removeFromCart}
                    total={getTotalPrice()}
                  />
                }
              />

              <Route
                path="/checkout"
                element={
                  <Checkout
                    cart={cart}
                    total={getTotalPrice()}
                    onSuccess={clearCart}
                  />
                }
              />

              <Route
                path="/orders"
                element={<OrderHistory />}
              />
            </Routes>
          </main>

          <footer className="App-footer">
            <p>&copy; 2024 E-Commerce Platform. Built with React & Spring Boot.</p>
          </footer>
        </div>
      </Router>
    </ErrorBoundary>
  );
}

export default App;