package com.datacloud.endpoint;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExchangeController {

    private final RabbitTemplate rabbit;
    private final RestTemplate http;

    public ExchangeController(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(3000);
        f.setReadTimeout(5000);
        this.http = new RestTemplate(f);
    }

    /** Camino asincrono: valida, encola y responde 202 de inmediato. */
    @PostMapping("/exchange")
    public ResponseEntity<?> exchange(@RequestBody ExchangeRequest r) {
        if (r.idPlatformOrigin() == null || r.idCoin() == null || r.idPlatformDestiny() == null
                || r.idProduct() == null || r.idUser() == null
                || r.Mount() == null || r.Mount() <= 0
                || r.Quantity() == null || r.Quantity() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Campos invalidos"));
        }
        try {
            rabbit.convertAndSend(QueueConfig.QUEUE, r);
            return ResponseEntity.status(202).body(Map.of("message", "Transaccion encolada"));
        } catch (AmqpException e) {
            return ResponseEntity.status(503).body(Map.of("error", "Cola no disponible"));
        }
    }

    /** Camino sincrono: consulta los coins pasando por el balanceador (devuelve X-Node). */
    @GetMapping("/coins/{idUser}")
    public ResponseEntity<String> coins(@PathVariable Integer idUser) {
        try {
            ResponseEntity<String> resp = http.getForEntity("http://balancer/api/coins/" + idUser, String.class);
            return reply(resp.getStatusCode().value(), resp.getBody(), resp.getHeaders().getFirst("X-Node"));
        } catch (HttpStatusCodeException e) {
            String node = e.getResponseHeaders() == null ? null : e.getResponseHeaders().getFirst("X-Node");
            return reply(e.getStatusCode().value(), e.getResponseBodyAsString(), node);
        } catch (ResourceAccessException e) {
            return reply(502, "{\"error\":\"balancer not available\"}", null);
        }
    }

    /** Compras rechazadas del usuario (sin coins tras todos los reintentos, producto inexistente, etc.). */
    @GetMapping("/failures/{idUser}")
    public ResponseEntity<String> failures(@PathVariable Integer idUser) {
        try {
            ResponseEntity<String> resp = http.getForEntity("http://balancer/api/failures/" + idUser, String.class);
            return reply(resp.getStatusCode().value(), resp.getBody(), resp.getHeaders().getFirst("X-Node"));
        } catch (HttpStatusCodeException e) {
            String node = e.getResponseHeaders() == null ? null : e.getResponseHeaders().getFirst("X-Node");
            return reply(e.getStatusCode().value(), e.getResponseBodyAsString(), node);
        } catch (ResourceAccessException e) {
            return reply(502, "{\"error\":\"balancer not available\"}", null);
        }
    }

    private ResponseEntity<String> reply(int status, String body, String node) {
        ResponseEntity.BodyBuilder b = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON);
        if (node != null) b.header("X-Node", node);
        return b.body(body);
    }
}
