package com.datacloud.endpoint;
import org.springframework.amqp.core.Queue;import org.springframework.context.annotation.*;
@Configuration public class QueueConfig {@Bean public Queue exchangeQueue(){return new Queue("exchange.requests",true);}}
