import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';

const ProductDetail = ({ addToCart }) => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchProduct();
  }, [id]);

  const fetchProduct = async () => {
    try {
      setLoading(true);
      const data = await api.getProduct(id);
      setProduct(data);
    } catch (error) {
      console.error('Error fetching product:', error);
      setError('Product not found');
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = () => {
    if (product && quantity > 0 && quantity <= product.stock) {
      addToCart(product, quantity);
      alert(`${quantity} ${product.name}(s) added to cart!`);
    }
  };

  const handleQuantityChange = (e) => {
    const newQuantity = parseInt(e.target.value);
    if (newQuantity >= 1 && newQuantity <= product.stock) {
      setQuantity(newQuantity);
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Loading product...</p>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="error-container">
        <h2>Product Not Found</h2>
        <p>{error || 'The requested product could not be found.'}</p>
        <button onClick={() => navigate('/')} className="back-button">
          Back to Products
        </button>
      </div>
    );
  }

  return (
    <div className="product-detail-container">
      <button onClick={() => navigate('/')} className="back-button">
        ← Back to Products
      </button>

      <div className="product-detail">
        <div className="product-image-section">
          <img
            src={product.imageUrl || '/placeholder-product.jpg'}
            alt={product.name}
            className="product-detail-image"
            onError={(e) => {
              e.target.src = '/placeholder-product.jpg';
            }}
          />
        </div>

        <div className="product-info-section">
          <h1 className="product-title">{product.name}</h1>

          <div className="product-price-section">
            <span className="product-price">${product.price}</span>
          </div>

          <div className="product-stock-section">
            {product.stock > 0 ? (
              <span className="in-stock">
                ✓ In Stock ({product.stock} available)
              </span>
            ) : (
              <span className="out-of-stock">
                ✗ Out of Stock
              </span>
            )}
          </div>

          <div className="product-description-section">
            <h3>Description</h3>
            <p className="product-description">{product.description}</p>
          </div>

          {product.stock > 0 && (
            <div className="purchase-section">
              <div className="quantity-selector">
                <label htmlFor="quantity">Quantity:</label>
                <select
                  id="quantity"
                  value={quantity}
                  onChange={handleQuantityChange}
                  className="quantity-select"
                >
                  {[...Array(Math.min(product.stock, 10))].map((_, i) => (
                    <option key={i + 1} value={i + 1}>
                      {i + 1}
                    </option>
                  ))}
                </select>
              </div>

              <button
                onClick={handleAddToCart}
                className="add-to-cart-detail-btn"
                disabled={product.stock === 0}
              >
                Add to Cart - ${(product.price * quantity).toFixed(2)}
              </button>
            </div>
          )}

          <div className="product-meta">
            <p><strong>Product ID:</strong> {product.id}</p>
            <p><strong>Added:</strong> {new Date(product.createdAt).toLocaleDateString()}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;