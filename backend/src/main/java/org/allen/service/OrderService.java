package org.allen.service;

import org.allen.entity.Order;
import org.allen.entity.OrderItem;
import org.allen.entity.OrderStatus;
import org.allen.entity.Product;
import org.allen.exception.BusinessException;
import org.allen.messaging.OrderMessageProducer;
import org.allen.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderMessageProducer messageProducer;

    public Order createOrder(Order order) {
        // Validate order
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BusinessException("Order must contain at least one item");
        }

        // Calculate total amount and validate stock
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            if (item.getQuantity() <= 0) {
                throw new BusinessException("Item quantity must be greater than 0");
            }
            
            Product product = item.getProduct();
            if (product == null) {
                throw new BusinessException("Product not found for item");
            }
            
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException("Insufficient stock for product: " + product.getName());
            }
            
            item.setOrder(order);
            totalAmount = totalAmount.add(item.getItemTotal());

            // Decrease product stock
            productService.decreaseStock(product.getId(), item.getQuantity());
        }
        
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Order total must be greater than 0");
        }
        
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        messageProducer.sendOrderCreated(savedOrder);
        return savedOrder;
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            Order updatedOrder = orderRepository.save(order);
            messageProducer.sendOrderStatusUpdated(updatedOrder);
            return updatedOrder;
        }
        return null;
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Order getOrderByStripePaymentIntentId(String paymentIntentId) {
        return orderRepository.findByStripePaymentIntentId(paymentIntentId);
    }

    public List<Order> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersBetweenDates(startDate, endDate);
    }

    public Long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public boolean cancelOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PROCESSING) {
                // Restore product stock
                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    product.setStock(product.getStock() + item.getQuantity());
                    productService.saveProduct(product);
                }

                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                messageProducer.sendOrderStatusUpdated(order);
                return true;
            }
        }
        return false;
    }
}