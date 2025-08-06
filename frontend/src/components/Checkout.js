import React, { useState } from 'react';
import PaymentForm from './PaymentForm';

const Checkout = ({ cart, total, onSuccess, onBack }) => {
  const [step, setStep] = useState(1);
  const [orderData, setOrderData] = useState({
    customerInfo: {
      name: '',
      email: '',
      address: '',
      city: '',
      zipCode: '',
    },
  });

  const handleCustomerInfoSubmit = (e) => {
    e.preventDefault();
    setStep(2);
  };

  const handleInputChange = (field, value) => {
    setOrderData({
      ...orderData,
      customerInfo: {
        ...orderData.customerInfo,
        [field]: value
      }
    });
  };

  return (
    <div className="checkout">
      <div className="checkout-header">
        <button onClick={onBack} className="back-btn">← Back to Cart</button>
        <h2>Checkout</h2>
      </div>

      {step === 1 && (
        <div className="customer-info-step">
          <h3>Customer Information</h3>
          <form onSubmit={handleCustomerInfoSubmit}>
            <div className="form-group">
              <label>Full Name:</label>
              <input
                type="text"
                required
                value={orderData.customerInfo.name}
                onChange={(e) => handleInputChange('name', e.target.value)}
              />
            </div>

            <div className="form-group">
              <label>Email:</label>
              <input
                type="email"
                required
                value={orderData.customerInfo.email}
                onChange={(e) => handleInputChange('email', e.target.value)}
              />
            </div>

            <div className="form-group">
              <label>Address:</label>
              <input
                type="text"
                required
                value={orderData.customerInfo.address}
                onChange={(e) => handleInputChange('address', e.target.value)}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>City:</label>
                <input
                  type="text"
                  required
                  value={orderData.customerInfo.city}
                  onChange={(e) => handleInputChange('city', e.target.value)}
                />
              </div>

              <div className="form-group">
                <label>ZIP Code:</label>
                <input
                  type="text"
                  required
                  value={orderData.customerInfo.zipCode}
                  onChange={(e) => handleInputChange('zipCode', e.target.value)}
                />
              </div>
            </div>

            <button type="submit" className="continue-btn">
              Continue to Payment
            </button>
          </form>
        </div>
      )}

      {step === 2 && (
        <PaymentForm
          cart={cart}
          total={total}
          customerInfo={orderData.customerInfo}
          onSuccess={onSuccess}
          onBack={() => setStep(1)}
        />
      )}
    </div>
  );
};

export default Checkout;