package org.allen.controller;

import com.stripe.model.PaymentIntent;
import org.allen.entity.Order;
import org.allen.entity.OrderStatus;
import org.allen.messaging.OrderMessageProducer;
import org.allen.service.OrderService;
import org.allen.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMessageProducer messageProducer;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(@RequestParam Long orderId) {
        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                PaymentIntent paymentIntent = paymentService.createPaymentIntent(order);

                Map<String, Object> response = paymentService.createPaymentIntentResponse(paymentIntent);

                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create payment intent");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/confirm-payment")
    public ResponseEntity<Map<String, Object>> confirmPayment(
            @RequestParam String paymentIntentId,
            @RequestParam(required = false) String paymentMethodId) {
        try {
            PaymentIntent paymentIntent;
            if (paymentMethodId != null) {
                paymentIntent = paymentService.confirmPaymentIntent(paymentIntentId, paymentMethodId);
            } else {
                paymentIntent = paymentService.confirmPaymentIntent(paymentIntentId);
            }

            // Update order based on payment status
            Order order = orderService.getOrderByStripePaymentIntentId(paymentIntentId);
            if (order != null) {
                messageProducer.sendPaymentProcessed(order, paymentIntent.getStatus());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", paymentIntent.getStatus());
            response.put("paymentIntentId", paymentIntent.getId());
            response.put("message", "Payment processed successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Payment failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/payment-intent/{paymentIntentId}")
    public ResponseEntity<Map<String, Object>> getPaymentIntent(@PathVariable String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = paymentService.retrievePaymentIntent(paymentIntentId);
            Map<String, Object> response = paymentService.createPaymentIntentResponse(paymentIntent);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/cancel-payment-intent")
    public ResponseEntity<Map<String, Object>> cancelPaymentIntent(@RequestParam String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = paymentService.cancelPaymentIntent(paymentIntentId);

            // Update associated order
            Order order = orderService.getOrderByStripePaymentIntentId(paymentIntentId);
            if (order != null) {
                orderService.updateOrderStatus(order.getId(), OrderStatus.CANCELLED);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", paymentIntent.getStatus());
            response.put("message", "Payment intent cancelled successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to cancel payment intent");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        // Handle Stripe webhooks for payment events
        // This would contain webhook verification and event processing

        try {
            // Process webhook events
            // Update order status based on payment events
            // Send notifications

            return ResponseEntity.ok("Webhook handled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook handling failed: " + e.getMessage());
        }
    }
}