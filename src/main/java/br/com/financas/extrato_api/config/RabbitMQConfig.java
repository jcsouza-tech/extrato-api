package br.com.financas.extrato_api.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ para processamento assíncrono
 */
@Configuration
public class RabbitMQConfig {

    // Nomes das filas e exchanges
    public static final String EXCHANGE_PROCESSAMENTO = "extrato.processamento";
    public static final String QUEUE_PROCESSAMENTO = "extrato.processamento.queue";
    public static final String QUEUE_STATUS = "extrato.status.queue";
    public static final String QUEUE_DLQ = "extrato.processamento.dlq";
    
    public static final String ROUTING_KEY_PROCESSAMENTO = "processamento";
    public static final String ROUTING_KEY_STATUS = "status";
    public static final String ROUTING_KEY_DLQ = "dlq";

    /**
     * Configuração do message converter para JSON
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configuração do RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * Configuração do listener container factory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(1);
        return factory;
    }

    /**
     * Exchange principal para processamento
     */
    @Bean
    public TopicExchange processamentoExchange() {
        return new TopicExchange(EXCHANGE_PROCESSAMENTO, true, false);
    }

    /**
     * Fila principal de processamento
     */
    @Bean
    public Queue processamentoQueue() {
        return QueueBuilder.durable(QUEUE_PROCESSAMENTO)
                .withArgument("x-dead-letter-exchange", EXCHANGE_PROCESSAMENTO)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DLQ)
                .withArgument("x-message-ttl", 300000) // 5 minutos TTL
                .build();
    }

    /**
     * Fila de status para notificações
     */
    @Bean
    public Queue statusQueue() {
        return QueueBuilder.durable(QUEUE_STATUS).build();
    }

    /**
     * Fila de Dead Letter Queue para mensagens com erro
     */
    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    /**
     * Binding da fila de processamento
     */
    @Bean
    public Binding processamentoBinding() {
        return BindingBuilder
                .bind(processamentoQueue())
                .to(processamentoExchange())
                .with(ROUTING_KEY_PROCESSAMENTO);
    }

    /**
     * Binding da fila de status
     */
    @Bean
    public Binding statusBinding() {
        return BindingBuilder
                .bind(statusQueue())
                .to(processamentoExchange())
                .with(ROUTING_KEY_STATUS);
    }

    /**
     * Binding da Dead Letter Queue
     */
    @Bean
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(dlqQueue())
                .to(processamentoExchange())
                .with(ROUTING_KEY_DLQ);
    }
}
