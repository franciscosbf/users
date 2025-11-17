package com.e_commerce.users;

import com.e_commerce.users.events.EmailUpdate;
import com.e_commerce.users.events.EventSender;
import com.e_commerce.users.model.EmailChange;
import com.e_commerce.users.model.UserCredentials;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.amqp.rabbit.test.RabbitListenerTestHarness;
import org.springframework.amqp.rabbit.test.mockito.LatchCountDownAndCallRealMethodAnswer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.RabbitMQContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@Import(TestcontainersConfiguration.class)
@SpringJUnitConfig
@DirtiesContext
public class UserServiceEventTests {
    public static class Listener {
        @RabbitListener(id = "receiveEmailUpdate", queues = "#{emailUpdateQueue.name}")
        public void receive(EmailUpdate ignoredUpdate) {

        }
    }

    @Configuration
    @RabbitListenerTest
    public static class RabbitConfiguration {
        private final UserRepository mockedRepository = mock(UserRepository.class);
        private final PasswordEncoder mockedPasswordEncoder = mock(PasswordEncoder.class);

        @Bean
        public UserRepository mockedRepository() {
            return mockedRepository;
        }

        @Bean
        public PasswordEncoder mockedPasswordEncoder() {
            return mockedPasswordEncoder;
        }

        @Bean
        public Listener buildListener() {
            return new Listener();
        }

        @Bean
        public ConnectionFactory connectionFactory(RabbitMQContainer container) {
            return new CachingConnectionFactory(container.getHost(), container.getAmqpPort());
        }

        @Bean
        public TopicExchange topicExchange() {
            return new TopicExchange(EventSender.RABBIT_TOPIC_EXCHANGE);
        }

        @Bean
        public Queue emailUpdateQueue() {
            return new AnonymousQueue();
        }

        @Bean
        public Binding bindEmailUpdateQueue(Queue emailUpdateQueue, TopicExchange topicExchange) {
            return BindingBuilder.bind(emailUpdateQueue).to(topicExchange)
                    .with(EventSender.RABBIT_UPDATE_EMAIL_ROUTING_KEY);
        }

        @Bean
        public Jackson2JsonMessageConverter messageConverter() {
            return new Jackson2JsonMessageConverter();
        }

        @Bean
        public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                             Jackson2JsonMessageConverter messageConverter) {
            RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

            rabbitTemplate.setMessageConverter(messageConverter);

            return rabbitTemplate;
        }

        @Bean
        public UserService userService(RabbitTemplate rabbitTemplate) {
            return new UserService(mockedRepository, mockedPasswordEncoder, new EventSender(rabbitTemplate));
        }

        @Bean
        public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
            return new RabbitAdmin(connectionFactory);
        }

        @Bean
        public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                                   Jackson2JsonMessageConverter messageConverter) {
            SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();

            containerFactory.setConnectionFactory(connectionFactory);
            containerFactory.setMessageConverter(messageConverter);

            return containerFactory;
        }
    }

    @Autowired
    private UserRepository mockedRepository;

    @Autowired
    private PasswordEncoder mockedPasswordEncoder;

    @Autowired
    private UserService service;

    @Autowired
    private RabbitListenerTestHarness harness;

    @Test
    public void emailUpdateEventIsSent() throws Exception {
        Listener listener = harness.getSpy("receiveEmailUpdate");
        assertThat(listener).isNotNull();

        when(mockedPasswordEncoder.encode("Password1@")).thenReturn("gibberish");
        when(mockedRepository.updateUserEmail("username", "gibberish", "new_username@email.com"))
                .thenReturn(1);

        LatchCountDownAndCallRealMethodAnswer answer = harness.getLatchAnswerFor("receiveEmailUpdate", 1);
        doAnswer(answer).when(listener).receive(any());

        EmailChange change = new EmailChange(
                new UserCredentials("username", "Password1@"), "new_username@email.com");

        service.updateUserEmail(change);

        assertTrue(answer.await(10));

        EmailUpdate update = new EmailUpdate("username", "new_username@email.com");

        verify(listener).receive(update);
    }
}
