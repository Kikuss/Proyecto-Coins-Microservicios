package com.datacloud.platform;
import org.springframework.http.*;import org.springframework.transaction.annotation.Transactional;import org.springframework.web.bind.annotation.*;
import jakarta.persistence.*;import java.util.Map;
@RestController @RequestMapping("/api") public class PurchaseController {
 @PersistenceContext EntityManager em;
 private final PurchaseRepository purchases;
 public PurchaseController(PurchaseRepository purchases){this.purchases=purchases;}
 @PostMapping("/purchase") @Transactional public ResponseEntity<?> purchase(@RequestBody ExchangeRequest r){
  if(r.idUser()==null||r.idProduct()==null||r.Mount()==null||r.Quantity()==null||r.Quantity()<=0||r.Mount()<=0)return ResponseEntity.badRequest().body(Map.of("error","Datos invalidos"));
  UserCoins user=em.find(UserCoins.class,r.idUser(),LockModeType.PESSIMISTIC_WRITE);
  Product product=em.find(Product.class,r.idProduct(),LockModeType.PESSIMISTIC_WRITE);
  if(user==null||product==null)return ResponseEntity.status(404).body(Map.of("error","Usuario o producto inexistente"));
  long total=(long)product.price*r.Quantity();
  if(total!=r.Mount())return ResponseEntity.badRequest().body(Map.of("error","Monto no coincide con precio por cantidad"));
  if(user.balance<total||product.stock<r.Quantity())return ResponseEntity.status(409).body(Map.of("error","Saldo o existencias insuficientes"));
  user.balance-=(int)total;product.stock-=r.Quantity();purchases.save(new Purchase(r));
  return ResponseEntity.ok(Map.of("message","Compra registrada","saldo",user.balance));
 }
 @GetMapping("/coins/{idUser}") public ResponseEntity<?> coins(@PathVariable Integer idUser){UserCoins u=em.find(UserCoins.class,idUser);return u==null?ResponseEntity.notFound().build():ResponseEntity.ok(Map.of("idUser",u.id,"coins",u.balance));}
}
