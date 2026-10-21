package com.datacloud.endpoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
@RestController @RequestMapping("/api") public class ExchangeController {
 private final RabbitTemplate rabbit; private final RestTemplate http = new RestTemplate();
 public ExchangeController(RabbitTemplate rabbit){this.rabbit=rabbit;}
 @PostMapping("/exchange") public ResponseEntity<?> exchange(@RequestBody ExchangeRequest r){
  if(r.idPlatformOrigin()==null||r.idCoin()==null||r.idPlatformDestiny()==null||r.idProduct()==null||r.idUser()==null||r.Mount()==null||r.Mount()<=0||r.Quantity()==null||r.Quantity()<=0) return ResponseEntity.badRequest().body(Map.of("error","Campos invalidos"));
  rabbit.convertAndSend("exchange.requests",r);return ResponseEntity.status(202).body(Map.of("message","Transaccion encolada"));
 }
 @GetMapping("/coins/{idUser}") public ResponseEntity<String> coins(@PathVariable Integer idUser){try {return ResponseEntity.ok(http.getForObject("http://balancer/api/coins/"+idUser,String.class));}catch(Exception e){return ResponseEntity.status(503).body("Servicio no disponible");}}
}
