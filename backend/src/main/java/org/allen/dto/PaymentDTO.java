package org.allen.dto;

import lombok.Data;

@Data
public class PaymentDTO {
    private String paymentIntentId;
    private String clientSecret;
    private String status;
    private Long amount;
    private String currency;
}
