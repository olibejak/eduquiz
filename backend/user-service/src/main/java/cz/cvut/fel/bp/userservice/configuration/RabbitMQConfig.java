package cz.cvut.fel.bp.userservice.configuration;

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

    @Value("${app.rabbitmq.exchange.name}")
    private String exchangeName;

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

    @Bean
    public TopicExchange domainExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue quizHistoryQueue() {
        return new Queue("user-service.quiz-history.queue", true);
    }

    @Bean
    public Binding quizHistoryBinding(Queue quizHistoryQueue, TopicExchange domainExchange) {
        return BindingBuilder
                .bind(quizHistoryQueue)
                .to(domainExchange)
                .with("quiz.ended");
    }
}
