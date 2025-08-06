const API_BASE_URL = process.env.REACT_APP_API_URL;

export const stripeService = {
  async createPaymentIntent(orderId) {
    const res = await fetch(`${API_BASE_URL}/payments/create-payment-intent?orderId=${orderId}`, {
      method: 'POST'
    });
    if (!res.ok) {
      throw new Error('Failed to create payment intent');
    }
    return res.json(); // 返回 PaymentDTO 对象
  },

  async confirmPayment(paymentIntentId, paymentMethodId) {
    const params = new URLSearchParams({ paymentIntentId });
    if (paymentMethodId) params.append('paymentMethodId', paymentMethodId);

    const res = await fetch(`${API_BASE_URL}/payments/confirm-payment?${params.toString()}`, {
      method: 'POST'
    });
    if (!res.ok) {
      throw new Error('Failed to confirm payment');
    }
    return res.json();
  },

  async cancelPaymentIntent(paymentIntentId) {
    const res = await fetch(`${API_BASE_URL}/payments/cancel-payment-intent?paymentIntentId=${paymentIntentId}`, {
      method: 'POST'
    });
    if (!res.ok) {
      throw new Error('Failed to cancel payment intent');
    }
    return res.json();
  },

  async retrievePaymentIntent(paymentIntentId) {
    const res = await fetch(`${API_BASE_URL}/payments/payment-intent/${paymentIntentId}`);
    if (!res.ok) {
      throw new Error('Failed to retrieve payment intent');
    }
    return res.json();
  }
};
