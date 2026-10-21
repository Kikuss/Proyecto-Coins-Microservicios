package com.datacloud.node;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/api")
public class NodeController {

    private final RestTemplate http = new RestTemplate();

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody ExchangeRequest r) {

        try {
            ResponseEntity<String> respuesta = http.postForEntity(
                "http://platform-destino:8090/api/purchase",
                r,
                String.class
            );

            return ResponseEntity.status(respuesta.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(respuesta.getBody());

        } catch (HttpClientErrorException e) {

            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        }
    }

    @GetMapping("/coins/{idUser}")
    public ResponseEntity<String> coins(@PathVariable Integer idUser) {

        ResponseEntity<String> respuesta = http.getForEntity(
            "http://platform-destino:8090/api/coins/" + idUser,
            String.class
        );

        return ResponseEntity.status(respuesta.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(respuesta.getBody());
    }
}
