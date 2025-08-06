import { api } from '../api';

// Mock fetch globally
global.fetch = jest.fn();

describe('API Service', () => {
  beforeEach(() => {
    fetch.mockClear();
  });

  describe('getProducts', () => {
    test('successfully fetches products', async () => {
      const mockProducts = [
        { id: 1, name: 'Product 1', price: 99.99 },
        { id: 2, name: 'Product 2', price: 149.99 }
      ];

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockProducts
      });

      const result = await api.getProducts();

      expect(result).toEqual(mockProducts);
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/products');
    });

    test('handles API error', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({ error: 'Internal server error' })
      });

      await expect(api.getProducts()).rejects.toThrow('HTTP error! status: 500');
    });

    test('handles network error', async () => {
      fetch.mockRejectedValueOnce(new Error('Network error'));

      await expect(api.getProducts()).rejects.toThrow('Network error');
    });
  });

  describe('getProduct', () => {
    test('successfully fetches single product', async () => {
      const mockProduct = { id: 1, name: 'Product 1', price: 99.99 };

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockProduct
      });

      const result = await api.getProduct(1);

      expect(result).toEqual(mockProduct);
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/products/1');
    });

    test('handles product not found', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
        json: async () => ({ error: 'Product not found' })
      });

      await expect(api.getProduct(999)).rejects.toThrow('HTTP error! status: 404');
    });
  });

  describe('searchProducts', () => {
    test('successfully searches products', async () => {
      const mockProducts = [
        { id: 1, name: 'iPhone', price: 999.99 }
      ];

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockProducts
      });

      const result = await api.searchProducts('iPhone');

      expect(result).toEqual(mockProducts);
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/products/search?q=iPhone');
    });

    test('handles special characters in search query', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => []
      });

      await api.searchProducts('iPhone & iPad');

      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/products/search?q=iPhone%20%26%20iPad');
    });
  });

  describe('createOrder', () => {
    test('successfully creates order', async () => {
      const orderData = {
        userId: 1,
        items: [
          { productId: 1, quantity: 2, price: 99.99 }
        ]
      };

      const mockOrder = {
        id: 1,
        totalAmount: 199.98,
        status: 'PENDING'
      };

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockOrder
      });

      const result = await api.createOrder(orderData);

      expect(result).toEqual(mockOrder);
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(orderData),
      });
    });

    test('handles order creation error', async () => {
      const orderData = { userId: 1, items: [] };

      fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ error: 'Invalid order data' })
      });

      await expect(api.createOrder(orderData)).rejects.toThrow('HTTP error! status: 400');
    });
  });

  describe('getOrder', () => {
    test('successfully fetches order', async () => {
      const mockOrder = {
        id: 1,
        totalAmount: 199.98,
        status: 'PENDING',
        items: []
      };

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockOrder
      });

      const result = await api.getOrder(1);

      expect(result).toEqual(mockOrder);
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/orders/1');
    });
  });

  describe('getUserOrders', () => {
    test('successfully fetches user orders', async () => {
      const mockOrders = [
        { id: 1, totalAmount: 199.98, status: 'PENDING' },
        { id: 2, totalAmount: 299.97, status: 'DELIVERED' }
      ];

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockOrders
      });

      const result = await api.getUserOrders(1);

      expect(result).toEqual(mockOrders);
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/orders/user/1');
    });
  });

  describe('createPaymentIntent', () => {
    test('successfully creates payment intent', async () => {
      const mockPaymentIntent = {
        paymentIntentId: 'pi_test123',
        clientSecret: 'pi_test123_secret',
        status: 'requires_payment_method'
      };

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockPaymentIntent
      });

      const result = await api.createPaymentIntent(1);

      expect(result).toEqual(mockPaymentIntent);
      expect(fetch).toHaveBeenCalledWith('http://localhost:8080/api/payments/create-payment-intent?orderId=1', {
        method: 'POST',
      });
    });

    test('handles payment intent creation error', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ error: 'Invalid order ID' })
      });

      await expect(api.createPaymentIntent(999)).rejects.toThrow('HTTP error! status: 400');
    });
  });

  describe('error handling', () => {
    test('handles JSON parsing error', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => {
          throw new Error('Invalid JSON');
        }
      });

      await expect(api.getProducts()).rejects.toThrow('HTTP error! status: 500');
    });

    test('handles empty response', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => null
      });

      const result = await api.getProducts();
      expect(result).toBeNull();
    });
  });

  describe('environment configuration', () => {
    const originalEnv = process.env;

    beforeEach(() => {
      jest.resetModules();
      process.env = { ...originalEnv };
    });

    afterEach(() => {
      process.env = originalEnv;
    });

    test('uses custom API URL from environment', async () => {
      process.env.REACT_APP_API_URL = 'https://custom-api.com/api';
      
      // Re-import the API module to get the new environment variable
      const { api: customApi } = require('../api');

      fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => []
      });

      await customApi.getProducts();

      expect(fetch).toHaveBeenCalledWith('https://custom-api.com/api/products');
    });
  });
}); 