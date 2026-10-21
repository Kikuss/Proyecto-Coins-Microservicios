package com.datacloud.worker;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class WorkerConsumer {

    private final RestTemplate http = new RestTemplate();

    @RabbitListener(queues = "exchange.requests")
    public void consume(ExchangeRequest r) {

        try {
            http.postForEntity(
                "http://balancer/api/transfer",
                r,
                String.class
            );

            System.out.println("Compra procesada correctamente");

        } catch (HttpClientErrorException e) {

            System.out.println(
                "Compra rechazada: " +
                e.getStatusCode() +
                " - " +
                e.getResponseBodyAsString()
            );

        }
    }
}
