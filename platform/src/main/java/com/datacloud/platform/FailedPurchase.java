package com.datacloud.platform;
import jakarta.persistence.*;import java.time.Instant;
@Entity @Table(name="compra_fallida") public class FailedPurchase {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Integer idPlatformOrigin,idCoin,idPlatformDestiny,idProduct,mount,quantity,idUser,attempts;
 public String reason;
 public Instant createdAt;
 public FailedPurchase(){}
}
