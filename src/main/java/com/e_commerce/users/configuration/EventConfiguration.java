package com.e_commerce.users.configuration;

import com.e_commerce.users.events.EventSender;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventConfiguration {
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EventSender.RABBIT_TOPIC_EXCHANGE);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(RabbitTemplate rabbit, TopicExchange topicExchange) {
        RabbitAdmin admin = new RabbitAdmin(rabbit);

        admin.declareExchange(topicExchange);

        return admin;
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
