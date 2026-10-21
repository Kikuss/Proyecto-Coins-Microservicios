package com.datacloud.worker;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.*;
@Configuration public class JsonConfig {@Bean public Jackson2JsonMessageConverter jsonConverter(){return new Jackson2JsonMessageConverter();}}
