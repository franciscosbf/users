package com.e_commerce.users.events;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventSender {
    public static final String RABBIT_TOPIC_EXCHANGE = "user";
    public static final String RABBIT_UPDATE_EMAIL_ROUTING_KEY = "update.email";

    private final RabbitTemplate rabbitTemplate;

    public EventSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    private void sendEvent(String routingKey, Object o) {
        rabbitTemplate.convertAndSend(RABBIT_TOPIC_EXCHANGE, routingKey, o);
    }

    public void sendEmailUpdate(EmailUpdate update) {
        sendEvent(RABBIT_UPDATE_EMAIL_ROUTING_KEY, update);
    }
}
