package com.e_commerce.users;

import com.e_commerce.users.events.EmailUpdate;
import com.e_commerce.users.events.EventSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EventSenderTests {
    @Mock
    private RabbitTemplate rabbit;

    @InjectMocks
    private EventSender eventSender;

    @Test
    public void emailUpdateEventIsSent() {
        EmailUpdate update = new EmailUpdate("username", "username@email.com");

        eventSender.sendEmailUpdate(update);

        verify(rabbit, times(1))
                .convertAndSend(EventSender.RABBIT_TOPIC_EXCHANGE, EventSender.RABBIT_UPDATE_EMAIL_ROUTING_KEY, update);
    }
}
