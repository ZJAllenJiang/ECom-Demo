package org.allen.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.allen.entity.Order;
import org.allen.entity.OrderStatus;
import org.allen.service.OrderService;
import org.allen.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class OrderMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderMessageConsumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @JmsListener(destination = "order.created")
    public void handleOrderCreated(String message) {
        try {
            Order order = objectMapper.readValue(message, Order.class);
            logger.info("Processing order created: {}", order.getId());

            // Process order creation business logic
            processNewOrder(order);

            // Send email notification (simulate)
            sendOrderConfirmationEmail(order);

            // Update order status to PROCESSING
            orderService.updateOrderStatus(order.getId(), OrderStatus.PROCESSING);

            logger.info("Successfully processed order created: {}", order.getId());
        } catch (Exception e) {
            logger.error("Error processing order created message: {}", message, e);
        }
    }

    @JmsListener(destination = "order.status.updated")
    public void handleOrderStatusUpdated(String message) {
        try {
            Order order = objectMapper.readValue(message, Order.class);
            logger.info("Processing order status update: {} -> {}", order.getId(), order.getStatus());

            // Process based on status
            switch (order.getStatus()) {
                case PROCESSING:
                    processOrderForShipping(order);
                    break;
                case SHIPPED:
                    sendShippingNotification(order);
                    break;
                case DELIVERED:
                    sendDeliveryConfirmation(order);
                    break;
                case CANCELLED:
                    processCancellation(order);
                    break;
                default:
                    break;
            }

            logger.info("Successfully processed order status update: {} -> {}",
                    order.getId(), order.getStatus());
        } catch (Exception e) {
            logger.error("Error processing order status updated message: {}", message, e);
        }
    }

    @JmsListener(destination = "order.cancelled")
    public void handleOrderCancelled(String message) {
        try {
            Order order = objectMapper.readValue(message, Order.class);
            logger.info("Processing order cancellation: {}", order.getId());

            // Process order cancellation
            processCancellation(order);

            // Send cancellation notification
            sendCancellationNotification(order);

            logger.info("Successfully processed order cancellation: {}", order.getId());
        } catch (Exception e) {
            logger.error("Error processing order cancelled message: {}", message, e);
        }
    }

    @JmsListener(destination = "payment.processed")
    public void handlePaymentProcessed(String message) {
        try {
            OrderMessageProducer.PaymentMessage paymentMessage =
                    objectMapper.readValue(message, OrderMessageProducer.PaymentMessage.class);

            Order order = paymentMessage.getOrder();
            String paymentStatus = paymentMessage.getPaymentStatus();

            logger.info("Processing payment: {} for order: {}", paymentStatus, order.getId());

            if ("succeeded".equals(paymentStatus)) {
                // Payment successful - update order status
                orderService.updateOrderStatus(order.getId(), OrderStatus.PROCESSING);
                sendPaymentConfirmationEmail(order);
            } else {
                // Payment failed - cancel order
                orderService.cancelOrder(order.getId());
                sendPaymentFailureNotification(order);
            }

            logger.info("Successfully processed payment: {} for order: {}", paymentStatus, order.getId());
        } catch (Exception e) {
            logger.error("Error processing payment message: {}", message, e);
        }
    }

    // Private helper methods
    private void processNewOrder(Order order) {
        // Business logic for new order processing
        logger.info("Processing new order business logic for order: {}", order.getId());

        // Validate inventory
        // Generate order number
        // Initialize tracking
    }

    private void processOrderForShipping(Order order) {
        // Prepare order for shipping
        logger.info("Preparing order for shipping: {}", order.getId());

        // Generate shipping label
        // Update inventory
        // Notify warehouse
    }

    private void processCancellation(Order order) {
        // Process order cancellation
        logger.info("Processing cancellation for order: {}", order.getId());

        // Restore inventory
        // Process refund if needed
        // Update analytics
    }

    private void sendOrderConfirmationEmail(Order order) {
        // Simulate sending email
        logger.info("Sending order confirmation email for order: {}", order.getId());
    }

    private void sendShippingNotification(Order order) {
        // Simulate sending shipping notification
        logger.info("Sending shipping notification for order: {}", order.getId());
    }

    private void sendDeliveryConfirmation(Order order) {
        // Simulate sending delivery confirmation
        logger.info("Sending delivery confirmation for order: {}", order.getId());
    }

    private void sendCancellationNotification(Order order) {
        // Simulate sending cancellation notification
        logger.info("Sending cancellation notification for order: {}", order.getId());
    }

    private void sendPaymentConfirmationEmail(Order order) {
        // Simulate sending payment confirmation
        logger.info("Sending payment confirmation email for order: {}", order.getId());
    }

    private void sendPaymentFailureNotification(Order order) {
        // Simulate sending payment failure notification
        logger.info("Sending payment failure notification for order: {}", order.getId());
    }
}