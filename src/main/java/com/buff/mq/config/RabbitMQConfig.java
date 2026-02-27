package com.buff.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置
 * <p>
 * 队列/交换机命名规范：buff.{业务域}.{事件}
 * 死信队列命名规范：buff.{业务域}.{事件}.dlq
 * @author Administrator
 */
@Configuration
public class RabbitMQConfig {

    // =================== 常量定义 ===================

    /** 订单确认收货 - 交换机 */
    public static final String ORDER_EXCHANGE = "buff.order.exchange";

    /** 订单确认收货 - 队列 */
    public static final String ORDER_CONFIRMED_QUEUE = "buff.order.confirmed";

    /** 订单确认收货 - 路由键 */
    public static final String ORDER_CONFIRMED_ROUTING_KEY = "order.confirmed";

    /** 死信交换机（处理消费失败的消息） */
    public static final String ORDER_DEAD_LETTER_EXCHANGE = "buff.order.dlx";

    /** 死信队列 */
    public static final String ORDER_CONFIRMED_DLQ = "buff.order.confirmed.dlq";

    // =================== 交换机 ===================

    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder.directExchange(ORDER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange orderDeadLetterExchange() {
        return ExchangeBuilder.directExchange(ORDER_DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }

    // =================== 队列 ===================

    @Bean
    public Queue orderConfirmedQueue() {
        return QueueBuilder.durable(ORDER_CONFIRMED_QUEUE)
                // 消费失败后转入死信队列
                .withArgument("x-dead-letter-exchange", ORDER_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_CONFIRMED_ROUTING_KEY + ".dlq")
                .build();
    }

    @Bean
    public Queue orderConfirmedDlq() {
        return QueueBuilder.durable(ORDER_CONFIRMED_DLQ).build();
    }

    // =================== 绑定 ===================

    @Bean
    public Binding orderConfirmedBinding(Queue orderConfirmedQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderConfirmedQueue)
                .to(orderExchange)
                .with(ORDER_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    public Binding orderConfirmedDlqBinding(Queue orderConfirmedDlq,
                                             DirectExchange orderDeadLetterExchange) {
        return BindingBuilder.bind(orderConfirmedDlq)
                .to(orderDeadLetterExchange)
                .with(ORDER_CONFIRMED_ROUTING_KEY + ".dlq");
    }

    // =================== 序列化 ===================

    /**
     * 使用 JSON 序列化消息体，便于跨语言消费和人工排查
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
