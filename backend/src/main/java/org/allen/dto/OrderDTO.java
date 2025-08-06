package org.allen.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}
