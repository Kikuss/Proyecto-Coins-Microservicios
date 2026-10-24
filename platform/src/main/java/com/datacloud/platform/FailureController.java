package com.datacloud.platform;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Compras que el worker no pudo completar (sin coins tras todos los intentos, o rechazo permanente). */
@RestController @RequestMapping("/api/failures") public class FailureController {
 public record FailureRequest(Integer idPlatformOrigin,Integer idCoin,Integer idPlatformDestiny,Integer idProduct,Integer Mount,Integer Quantity,Integer idUser,String reason,Integer attempts){}
 private final FailedPurchaseRepository failures;
 public FailureController(FailedPurchaseRepository failures){this.failures=failures;}

 @PostMapping public ResponseEntity<?> register(@RequestBody FailureRequest r){
  if(r==null||r.idUser()==null)return ResponseEntity.badRequest().body(Map.of("error","Datos invalidos"));
  FailedPurchase f=new FailedPurchase();
  f.idPlatformOrigin=r.idPlatformOrigin();f.idCoin=r.idCoin();f.idPlatformDestiny=r.idPlatformDestiny();
  f.idProduct=r.idProduct();f.mount=r.Mount();f.quantity=r.Quantity();f.idUser=r.idUser();
  f.attempts=r.attempts();f.reason=r.reason();f.createdAt=Instant.now();
  failures.save(f);
  return ResponseEntity.status(201).body(Map.of("message","Compra rechazada registrada","id",f.id));
 }
 @GetMapping public List<FailedPurchase> all(){return failures.findAll();}
 @GetMapping("/{idUser}") public List<FailedPurchase> byUser(@PathVariable Integer idUser){return failures.findByIdUserOrderByIdDesc(idUser);}
}
