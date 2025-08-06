package org.allen.service;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentConfirmParams;
import org.allen.entity.Order;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    public PaymentIntent createPaymentIntent(Order order) throws Exception {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) (order.getTotalAmount().doubleValue() * 100)) // Convert to cents
                .setCurrency("usd")
                .addPaymentMethodType("card")
                .putMetadata("order_id", order.getId().toString())
                .setDescription("Order #" + order.getId())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Update order with payment intent ID
        order.setStripePaymentIntentId(paymentIntent.getId());

        return paymentIntent;
    }

    public PaymentIntent confirmPaymentIntent(String paymentIntentId) throws Exception {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.confirm();
    }

    public PaymentIntent confirmPaymentIntent(String paymentIntentId, String paymentMethodId) throws Exception {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
                .setPaymentMethod(paymentMethodId)
                .build();

        return paymentIntent.confirm(params);
    }

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws Exception {
        return PaymentIntent.retrieve(paymentIntentId);
    }

    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws Exception {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.cancel();
    }

    public Map<String, Object> createPaymentIntentResponse(PaymentIntent paymentIntent) {
        Map<String, Object> response = new HashMap<>();
        response.put("paymentIntentId", paymentIntent.getId());
        response.put("clientSecret", paymentIntent.getClientSecret());
        response.put("status", paymentIntent.getStatus());
        response.put("amount", paymentIntent.getAmount());
        response.put("currency", paymentIntent.getCurrency());
        return response;
    }
}