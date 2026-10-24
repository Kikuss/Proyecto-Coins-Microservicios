package com.datacloud.endpoint;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.*;
@Configuration public class QueueConfig {
 public static final String QUEUE="exchange.requests";
 public static final String DLQ="exchange.requests.dlq";
 @Bean public Queue exchangeQueue(){return QueueBuilder.durable(QUEUE).withArgument("x-dead-letter-exchange","").withArgument("x-dead-letter-routing-key",DLQ).build();}
 @Bean public Queue exchangeDlq(){return QueueBuilder.durable(DLQ).build();}
}
