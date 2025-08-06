import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import './OrderHistory.css';

const OrderHistory = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      // For demo purposes, using a hardcoded user ID
      // In a real app, this would come from authentication context
      const userId = 1;
      const data = await api.getUserOrders(userId);
      setOrders(data);
    } catch (error) {
      console.error('Error fetching orders:', error);
      setError('Failed to load order history');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusColor = (status) => {
    const statusColors = {
      'PENDING': '#ffa500',
      'PROCESSING': '#007bff',
      'SHIPPED': '#17a2b8',
      'DELIVERED': '#28a745',
      'CANCELLED': '#dc3545',
      'REFUNDED': '#6c757d'
    };
    return statusColors[status] || '#6c757d';
  };

  if (loading) {
    return (
      <div className="order-history-container">
        <div className="loading-spinner"></div>
        <p>Loading order history...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="order-history-container">
        <div className="error-message">
          <h2>Error</h2>
          <p>{error}</p>
          <button onClick={fetchOrders} className="retry-button">
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="order-history-container">
      <h1>Order History</h1>
      
      {orders.length === 0 ? (
        <div className="no-orders">
          <p>No orders found.</p>
          <p>Start shopping to see your order history here!</p>
        </div>
      ) : (
        <div className="orders-list">
          {orders.map((order) => (
            <div key={order.id} className="order-card">
              <div className="order-header">
                <h3>Order #{order.id}</h3>
                <span 
                  className="order-status"
                  style={{ backgroundColor: getStatusColor(order.status) }}
                >
                  {order.status}
                </span>
              </div>
              
              <div className="order-details">
                <p><strong>Date:</strong> {formatDate(order.createdAt)}</p>
                <p><strong>Total:</strong> ${order.totalAmount}</p>
                {order.stripePaymentIntentId && (
                  <p><strong>Payment ID:</strong> {order.stripePaymentIntentId}</p>
                )}
              </div>

              {order.items && order.items.length > 0 && (
                <div className="order-items">
                  <h4>Items:</h4>
                  <div className="items-list">
                    {order.items.map((item) => (
                      <div key={item.id} className="order-item">
                        <span className="item-name">{item.product?.name || 'Product'}</span>
                        <span className="item-quantity">x{item.quantity}</span>
                        <span className="item-price">${item.price}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default OrderHistory; 