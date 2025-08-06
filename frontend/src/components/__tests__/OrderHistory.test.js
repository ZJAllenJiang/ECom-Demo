import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import OrderHistory from '../OrderHistory';
import { api } from '../../services/api';

// Mock the API service
jest.mock('../../services/api');

describe('OrderHistory Component', () => {
  const mockOrders = [
    {
      id: 1,
      totalAmount: 199.98,
      status: 'PENDING',
      createdAt: '2024-01-15T10:30:00Z',
      stripePaymentIntentId: 'pi_test123',
      items: [
        {
          id: 1,
          quantity: 2,
          price: 99.99,
          product: {
            id: 1,
            name: 'Test Product',
            price: 99.99
          }
        }
      ]
    },
    {
      id: 2,
      totalAmount: 299.97,
      status: 'DELIVERED',
      createdAt: '2024-01-14T15:45:00Z',
      items: [
        {
          id: 2,
          quantity: 3,
          price: 99.99,
          product: {
            id: 1,
            name: 'Test Product',
            price: 99.99
          }
        }
      ]
    }
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders loading state initially', () => {
    api.getUserOrders.mockImplementation(() => new Promise(() => {}));
    
    render(<OrderHistory />);
    
    expect(screen.getByText('Loading order history...')).toBeInTheDocument();
  });

  test('renders orders successfully', async () => {
    api.getUserOrders.mockResolvedValue(mockOrders);
    
    render(<OrderHistory />);
    
    await waitFor(() => {
      expect(screen.getByText('Order History')).toBeInTheDocument();
    });
    
    expect(screen.getByText('Order #1')).toBeInTheDocument();
    expect(screen.getByText('Order #2')).toBeInTheDocument();
    expect(screen.getByText('PENDING')).toBeInTheDocument();
    expect(screen.getByText('DELIVERED')).toBeInTheDocument();
    expect(screen.getByText('$199.98')).toBeInTheDocument();
    expect(screen.getByText('$299.97')).toBeInTheDocument();
  });

  test('renders empty state when no orders', async () => {
    api.getUserOrders.mockResolvedValue([]);
    
    render(<OrderHistory />);
    
    await waitFor(() => {
      expect(screen.getByText('No orders found.')).toBeInTheDocument();
      expect(screen.getByText('Start shopping to see your order history here!')).toBeInTheDocument();
    });
  });

  test('renders error state when API fails', async () => {
    api.getUserOrders.mockRejectedValue(new Error('API Error'));
    
    render(<OrderHistory />);
    
    await waitFor(() => {
      expect(screen.getByText('Error')).toBeInTheDocument();
      expect(screen.getByText('Failed to load order history')).toBeInTheDocument();
      expect(screen.getByText('Retry')).toBeInTheDocument();
    });
  });

  test('displays order details correctly', async () => {
    api.getUserOrders.mockResolvedValue([mockOrders[0]]);
    
    render(<OrderHistory />);
    
    await waitFor(() => {
      expect(screen.getByText('Order #1')).toBeInTheDocument();
      expect(screen.getByText('PENDING')).toBeInTheDocument();
      expect(screen.getByText('$199.98')).toBeInTheDocument();
      expect(screen.getByText('pi_test123')).toBeInTheDocument();
    });
  });

  test('displays order items correctly', async () => {
    api.getUserOrders.mockResolvedValue([mockOrders[0]]);
    
    render(<OrderHistory />);
    
    await waitFor(() => {
      expect(screen.getByText('Items:')).toBeInTheDocument();
      expect(screen.getByText('Test Product')).toBeInTheDocument();
      expect(screen.getByText('x2')).toBeInTheDocument();
      expect(screen.getByText('$99.99')).toBeInTheDocument();
    });
  });

  test('handles missing product data gracefully', async () => {
    const orderWithMissingProduct = {
      ...mockOrders[0],
      items: [
        {
          id: 1,
          quantity: 2,
          price: 99.99,
          product: null
        }
      ]
    };
    
    api.getUserOrders.mockResolvedValue([orderWithMissingProduct]);
    
    render(<OrderHistory />);
    
    await waitFor(() => {
      expect(screen.getByText('Product')).toBeInTheDocument(); // Fallback text
    });
  });

  test('retry button works when error occurs', async () => {
    api.getUserOrders
      .mockRejectedValueOnce(new Error('API Error'))
      .mockResolvedValueOnce(mockOrders);
    
    render(<OrderHistory />);
    
    await waitFor(() => {
      expect(screen.getByText('Error')).toBeInTheDocument();
    });
    
    const retryButton = screen.getByText('Retry');
    retryButton.click();
    
    await waitFor(() => {
      expect(screen.getByText('Order #1')).toBeInTheDocument();
    });
    
    expect(api.getUserOrders).toHaveBeenCalledTimes(2);
  });

  test('formats date correctly', async () => {
    api.getUserOrders.mockResolvedValue([mockOrders[0]]);
    
    render(<OrderHistory />);
    
    await waitFor(() => {
      // Check if the date is formatted (should contain month name)
      const dateText = screen.getByText(/January 15, 2024/);
      expect(dateText).toBeInTheDocument();
    });
  });

  test('applies correct status colors', async () => {
    api.getUserOrders.mockResolvedValue(mockOrders);
    
    render(<OrderHistory />);
    
    await waitFor(() => {
      const pendingStatus = screen.getByText('PENDING');
      const deliveredStatus = screen.getByText('DELIVERED');
      
      expect(pendingStatus).toHaveStyle('background-color: #ffa500');
      expect(deliveredStatus).toHaveStyle('background-color: #28a745');
    });
  });
}); 