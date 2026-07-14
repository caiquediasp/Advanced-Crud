package com.caique.advancedcrud.shared.errorlog;

import com.caique.advancedcrud.shared.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ErrorLogPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ErrorLogPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(CriticalErrorEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.ERROR_EXCHANGE,
                RabbitConfig.ERROR_ROUTING_KEY,
                event);
    }
}
