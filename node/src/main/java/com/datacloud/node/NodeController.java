package com.datacloud.node;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class NodeController {

    private final String nodeName;
    private final String platformUrl;
    private final RestTemplate http;

    public NodeController(@Value("${NODE_NAME:node}") String nodeName,
                          @Value("${platform.url}") String platformUrl) {
        this.nodeName = nodeName;
        this.platformUrl = platformUrl;
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(2000);
        f.setReadTimeout(4000);
        this.http = new RestTemplate(f);
    }

    /** Recibe la compra del worker (via balanceador) y la reenvia a la plataforma destino. */
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody ExchangeRequest r) {
        try {
            ResponseEntity<String> resp = http.postForEntity(platformUrl + "/api/purchase", r, String.class);
            return reply(resp.getStatusCode().value(), resp.getBody());
        } catch (HttpStatusCodeException e) {
            return reply(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            return reply(502, "{\"error\":\"platform not available\"}");
        }
    }

    @GetMapping("/coins/{idUser}")
    public ResponseEntity<String> coins(@PathVariable Integer idUser) {
        try {
            ResponseEntity<String> resp = http.getForEntity(platformUrl + "/api/coins/" + idUser, String.class);
            return reply(resp.getStatusCode().value(), resp.getBody());
        } catch (HttpStatusCodeException e) {
            return reply(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            return reply(502, "{\"error\":\"platform not available\"}");
        }
    }

    /** El worker registra aqui una compra que no se pudo completar. */
    @PostMapping("/failures")
    public ResponseEntity<String> registerFailure(@RequestBody String json) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<String> resp = http.postForEntity(platformUrl + "/api/failures", new HttpEntity<>(json, h), String.class);
            return reply(resp.getStatusCode().value(), resp.getBody());
        } catch (HttpStatusCodeException e) {
            return reply(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            return reply(502, "{\"error\":\"platform not available\"}");
        }
    }

    @GetMapping("/failures/{idUser}")
    public ResponseEntity<String> failures(@PathVariable Integer idUser) {
        try {
            ResponseEntity<String> resp = http.getForEntity(platformUrl + "/api/failures/" + idUser, String.class);
            return reply(resp.getStatusCode().value(), resp.getBody());
        } catch (HttpStatusCodeException e) {
            return reply(e.getStatusCode().value(), e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            return reply(502, "{\"error\":\"platform not available\"}");
        }
    }

    /** Sin base de datos: sirve para comprobar el reparto del balanceador. */
    @GetMapping("/node/info")
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok().header("X-Node", nodeName).body(Map.of("node", nodeName));
    }

    private ResponseEntity<String> reply(int status, String body) {
        return ResponseEntity.status(status)
                .header("X-Node", nodeName)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}
