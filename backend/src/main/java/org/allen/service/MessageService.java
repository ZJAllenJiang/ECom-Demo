package org.allen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    // 示例：发送订单确认邮件
    public void sendOrderConfirmation(String email, String orderNumber) {
        // 实际项目可集成 JavaMailSender 或第三方邮件服务
        logger.info("Sending order confirmation to {} for order {}", email, orderNumber);
        // TODO: 调用邮件发送服务
    }

    // 示例：发送支付成功通知
    public void sendPaymentSuccess(String email, String orderNumber) {
        logger.info("Sending payment success email to {} for order {}", email, orderNumber);
        // TODO: 调用邮件/短信服务
    }
}
