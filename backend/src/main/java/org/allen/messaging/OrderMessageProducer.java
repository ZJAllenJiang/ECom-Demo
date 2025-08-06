package org.allen.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.allen.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderMessageProducer.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendOrderCreated(Order order) {
        try {
            String message = objectMapper.writeValueAsString(order);
            jmsTemplate.convertAndSend("order.created", message);
            logger.info("Sent order created message for order ID: {}", order.getId());
        } catch (Exception e) {
            logger.error("Error sending order created message for order ID: {}", order.getId(), e);
        }
    }

    public void sendOrderStatusUpdated(Order order) {
        try {
            String message = objectMapper.writeValueAsString(order);
            jmsTemplate.convertAndSend("order.status.updated", message);
            logger.info("Sent order status updated message for order ID: {} with status: {}",
                    order.getId(), order.getStatus());
        } catch (Exception e) {
            logger.error("Error sending order status updated message for order ID: {}",
                    order.getId(), e);
        }
    }

    public void sendOrderCancelled(Order order) {
        try {
            String message = objectMapper.writeValueAsString(order);
            jmsTemplate.convertAndSend("order.cancelled", message);
            logger.info("Sent order cancelled message for order ID: {}", order.getId());
        } catch (Exception e) {
            logger.error("Error sending order cancelled message for order ID: {}", order.getId(), e);
        }
    }

    public void sendPaymentProcessed(Order order, String paymentStatus) {
        try {
            PaymentMessage paymentMessage = new PaymentMessage(order, paymentStatus);
            String message = objectMapper.writeValueAsString(paymentMessage);
            jmsTemplate.convertAndSend("payment.processed", message);
            logger.info("Sent payment processed message for order ID: {} with status: {}",
                    order.getId(), paymentStatus);
        } catch (Exception e) {
            logger.error("Error sending payment processed message for order ID: {}",
                    order.getId(), e);
        }
    }

    // Inner class for payment messages
    public static class PaymentMessage {
        private Order order;
        private String paymentStatus;

        public PaymentMessage() {}

        public PaymentMessage(Order order, String paymentStatus) {
            this.order = order;
            this.paymentStatus = paymentStatus;
        }

        // Getters and setters
        public Order getOrder() { return order; }
        public void setOrder(Order order) { this.order = order; }

        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    }
}