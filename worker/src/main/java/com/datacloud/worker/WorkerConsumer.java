package com.datacloud.worker;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * ACK MANUAL + cola de espera con TTL (el hilo NO se duerme):
 *  - compra OK                                  -> ack.
 *  - 402 (sin coins), 429, 5xx o caida de red   -> el mensaje pasa a "exchange.retry"; cuando vence su TTL
 *                                                  RabbitMQ lo devuelve a "exchange.requests" y se reintenta.
 *  - otros 4xx (400, 404, 409 sin stock)        -> error permanente, directo a la DLQ.
 *  - intentos agotados                          -> DLQ (nunca se pierde el mensaje).
 * El numero de intentos viaja en el header "x-attempts".
 */
@Component
public class WorkerConsumer {

    private static final Logger log = LoggerFactory.getLogger(WorkerConsumer.class);

    private final RestTemplate http;
    private final RabbitTemplate rabbit;
    private final String url;
    private final String failedUrl;
    private final int maxAttempts;

    public WorkerConsumer(RabbitTemplate rabbit,
                          @Value("${balancer.url}") String url,
                          @Value("${balancer.failed-url}") String failedUrl,
                          @Value("${worker.max-attempts}") int maxAttempts) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(3000);
        f.setReadTimeout(5000);
        this.http = new RestTemplate(f);
        this.rabbit = rabbit;
        this.url = url;
        this.failedUrl = failedUrl;
        this.maxAttempts = maxAttempts;
    }

    @RabbitListener(queues = QueueConfig.QUEUE)
    public void consume(ExchangeRequest r, Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                        @Header(name = "x-attempts", required = false) Integer previousAttempts) throws IOException {
        int attempt = (previousAttempts == null ? 0 : previousAttempts) + 1;
        try {
            http.postForEntity(url, r, String.class);
            channel.basicAck(tag, false);
            log.info("Compra procesada correctamente (intento {}): {}", attempt, r);
            return;
        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            if (status != 402 && status != 429) {
                log.error("Rechazo permanente {} -> DLQ: {} {}", status, r, e.getResponseBodyAsString());
                reportFailure(r, "Rechazo permanente HTTP " + status + ": " + e.getResponseBodyAsString(), attempt);
                channel.basicNack(tag, false, false);
                return;
            }
            log.warn("Intento {}/{} sin coins ({}), a la cola de espera: {}", attempt, maxAttempts, status, r);
        } catch (RestClientException e) {
            log.warn("Intento {}/{} fallo ({}), a la cola de espera: {}", attempt, maxAttempts, e.getMessage(), r);
        }

        if (attempt >= maxAttempts) {
            log.error("Intentos agotados -> DLQ: {}", r);
            reportFailure(r, "Compra rechazada: sin coins (o servicio no disponible) tras " + attempt + " intentos", attempt);
            channel.basicNack(tag, false, false);
            return;
        }
        try {
            // Primero se publica en la cola de espera y despues se confirma el original: no se pierde nada.
            rabbit.convertAndSend("", QueueConfig.RETRY, r, m -> {
                m.getMessageProperties().setHeader("x-attempts", attempt);
                return m;
            });
            channel.basicAck(tag, false);
        } catch (AmqpException ex) {
            log.error("No se pudo mover a la cola de espera, se devuelve a la cola: {}", ex.getMessage());
            channel.basicNack(tag, false, true);
        }
    }

    /** Deja constancia de la compra rechazada en la plataforma. Si falla, igual se manda a la DLQ. */
    private void reportFailure(ExchangeRequest r, String reason, int attempts) {
        try {
            http.postForEntity(failedUrl, FailureReport.of(r, reason, attempts), String.class);
        } catch (RestClientException e) {
            log.error("No se pudo registrar la compra rechazada ({}): {}", e.getMessage(), r);
        }
    }
}
