package com.caique.advancedcrud.shared.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String ERROR_EXCHANGE = "error.exchange";
    public static final String ERROR_QUEUE = "error.queue";
    public static final String ERROR_ROUTING_KEY = "error.critical";
    public static final String ERROR_DLQ = "error.queue.dlq";
    public static final String ERROR_DLX = "error.exchange.dlx";

    @Bean
    DirectExchange errorExchange() {
        return new DirectExchange(ERROR_EXCHANGE);
    }

    @Bean
    Queue errorQueue() {
        return QueueBuilder.durable(ERROR_QUEUE)
                .withArgument("x-dead-letter-exchange", ERROR_DLX)
                .withArgument("x-dead-letter-routing-key", ERROR_DLQ).build();
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(ERROR_DLX);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(ERROR_DLQ).build();
    }

    @Bean
    Binding errorBinding(Queue errorQueue, DirectExchange errorExchange) {
        return BindingBuilder.bind(errorQueue)
                .to(errorExchange)
                .with(ERROR_ROUTING_KEY);
    }

    @Bean
    Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(ERROR_DLQ);
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

}
