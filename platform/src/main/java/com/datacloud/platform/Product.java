package com.datacloud.platform;
import jakarta.persistence.*;
@Entity @Table(name="producto") public class Product {
 @Id public Integer id; public String name; public Integer price; public Integer stock;
 public Product(){} public Product(Integer id,String name,Integer price,Integer stock){this.id=id;this.name=name;this.price=price;this.stock=stock;}
}
