package com.datacloud.platform;
import jakarta.persistence.*;import java.time.Instant;
@Entity @Table(name="compra_detalle") public class Purchase {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Integer idPlatformOrigin,idCoin,idPlatformDestiny,idProduct,mount,quantity,idUser;
 public Instant createdAt;
 public Purchase(){} public Purchase(ExchangeRequest r){idPlatformOrigin=r.idPlatformOrigin();idCoin=r.idCoin();idPlatformDestiny=r.idPlatformDestiny();idProduct=r.idProduct();mount=r.Mount();quantity=r.Quantity();idUser=r.idUser();createdAt=Instant.now();}
}
