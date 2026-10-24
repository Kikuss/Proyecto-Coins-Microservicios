package com.datacloud.worker;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration public class QueueConfig {
 public static final String QUEUE="exchange.requests";
 public static final String RETRY="exchange.retry";
 public static final String DLQ="exchange.requests.dlq";

 /** Cola principal: lo rechazado sin reintento va a la DLQ. (Mismos argumentos que en el endpoint.) */
 @Bean public Queue exchangeQueue(){return QueueBuilder.durable(QUEUE).withArgument("x-dead-letter-exchange","").withArgument("x-dead-letter-routing-key",DLQ).build();}

 /** Cola de espera: nadie la consume; al vencer el TTL RabbitMQ devuelve el mensaje a la cola principal. */
 @Bean public Queue retryQueue(@Value("${worker.retry-delay-ms}") long delayMs){
  return QueueBuilder.durable(RETRY).withArgument("x-message-ttl",delayMs).withArgument("x-dead-letter-exchange","").withArgument("x-dead-letter-routing-key",QUEUE).build();
 }

 @Bean public Queue exchangeDlq(){return QueueBuilder.durable(DLQ).build();}
}
