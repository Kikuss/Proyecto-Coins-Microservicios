package com.datacloud.platform;
import jakarta.persistence.*;
@Entity @Table(name="usuario_coins") public class UserCoins {
 @Id public Integer id; public Integer balance;
 public UserCoins(){} public UserCoins(Integer id,Integer balance){this.id=id;this.balance=balance;}
}
