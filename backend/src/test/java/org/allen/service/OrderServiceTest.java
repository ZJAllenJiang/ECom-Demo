package org.allen.service;

import org.allen.entity.Order;
import org.allen.entity.OrderItem;
import org.allen.entity.OrderStatus;
import org.allen.entity.Product;
import org.allen.exception.BusinessException;
import org.allen.messaging.OrderMessageProducer;
import org.allen.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private OrderMessageProducer messageProducer;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private Product testProduct;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStock(10);

        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(2);
        testOrderItem.setPrice(new BigDecimal("99.99"));

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setItems(Arrays.asList(testOrderItem));
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(productService).decreaseStock(anyLong(), anyInt());
        doNothing().when(messageProducer).sendOrderCreated(any(Order.class));

        // Act
        Order result = orderService.createOrder(testOrder);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertNotNull(result.getCreatedAt());
        assertTrue(result.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
        verify(productService).decreaseStock(1L, 2);
        verify(messageProducer).sendOrderCreated(testOrder);
        verify(orderRepository).save(testOrder);
    }

    @Test
    void testCreateOrder_EmptyItems_ThrowsException() {
        // Arrange
        testOrder.setItems(new ArrayList<>());

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> orderService.createOrder(testOrder));
        assertEquals("Order must contain at least one item", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_NullItems_ThrowsException() {
        // Arrange
        testOrder.setItems(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> orderService.createOrder(testOrder));
        assertEquals("Order must contain at least one item", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_ZeroQuantity_ThrowsException() {
        // Arrange
        testOrderItem.setQuantity(0);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> orderService.createOrder(testOrder));
        assertEquals("Item quantity must be greater than 0", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_NegativeQuantity_ThrowsException() {
        // Arrange
        testOrderItem.setQuantity(-1);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> orderService.createOrder(testOrder));
        assertEquals("Item quantity must be greater than 0", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_NullProduct_ThrowsException() {
        // Arrange
        testOrderItem.setProduct(null);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> orderService.createOrder(testOrder));
        assertEquals("Product not found for item", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_InsufficientStock_ThrowsException() {
        // Arrange
        testProduct.setStock(1);
        testOrderItem.setQuantity(5);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> orderService.createOrder(testOrder));
        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetOrderById_Exists() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        Optional<Order> result = orderService.getOrderById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testOrder, result.get());
        verify(orderRepository).findById(1L);
    }

    @Test
    void testGetOrderById_NotExists() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = orderService.getOrderById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(orderRepository).findById(999L);
    }

    @Test
    void testGetOrdersByUserId() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(orders);

        // Act
        List<Order> result = orderService.getOrdersByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void testGetAllOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        // Act
        List<Order> result = orderService.getAllOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void testUpdateOrderStatus_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doNothing().when(messageProducer).sendOrderStatusUpdated(any(Order.class));

        // Act
        Order result = orderService.updateOrderStatus(1L, OrderStatus.SHIPPED);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.SHIPPED, result.getStatus());
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(testOrder);
        verify(messageProducer).sendOrderStatusUpdated(testOrder);
    }

    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Order result = orderService.updateOrderStatus(999L, OrderStatus.SHIPPED);

        // Assert
        assertNull(result);
        verify(orderRepository).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetOrdersByStatus() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(orders);

        // Act
        List<Order> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findByStatus(OrderStatus.PENDING);
    }

    @Test
    void testGetOrderByStripePaymentIntentId() {
        // Arrange
        String paymentIntentId = "pi_test123";
        when(orderRepository.findByStripePaymentIntentId(paymentIntentId)).thenReturn(testOrder);

        // Act
        Order result = orderService.getOrderByStripePaymentIntentId(paymentIntentId);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder, result);
        verify(orderRepository).findByStripePaymentIntentId(paymentIntentId);
    }

    @Test
    void testCancelOrder_Success() {
        // Arrange
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(productService.saveProduct(any(Product.class))).thenReturn(testProduct);
        doNothing().when(messageProducer).sendOrderStatusUpdated(any(Order.class));

        // Act
        boolean result = orderService.cancelOrder(1L);

        // Assert
        assertTrue(result);
        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(testOrder);
        verify(productService).saveProduct(any(Product.class));
        verify(messageProducer).sendOrderStatusUpdated(testOrder);
    }

    @Test
    void testCancelOrder_OrderNotFound() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean result = orderService.cancelOrder(999L);

        // Assert
        assertFalse(result);
        verify(orderRepository).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCancelOrder_AlreadyDelivered() {
        // Arrange
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        boolean result = orderService.cancelOrder(1L);

        // Assert
        assertFalse(result);
        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }
} 