package cz.cvut.fel.bp.deckservice.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange.deck}")
    private String deckExchangeName;

    @Value("${app.rabbitmq.exchange.user}")
    private String userExchangeName;

    @Value("${app.rabbitmq.queue.user-deleted}")
    private String userDeletedQueueName;

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter("cz.cvut.fel.bp.*");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    // ##############
    // DECK
    // ##############

    @Bean
    public TopicExchange deckExchange() {
        return new TopicExchange(deckExchangeName);
    }

    // ###########
    // USER
    // ###########

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(userExchangeName);
    }

    @Bean
    public Queue deckUserDeletedQueue() {
        return new Queue(userDeletedQueueName, true);
    }

    @Bean
    public Binding deckUserDeletedBinding(Queue deckUserDeletedQueue, TopicExchange userExchange) {
        return BindingBuilder
                .bind(deckUserDeletedQueue)
                .to(userExchange)
                .with("user.deleted");
    }
}
