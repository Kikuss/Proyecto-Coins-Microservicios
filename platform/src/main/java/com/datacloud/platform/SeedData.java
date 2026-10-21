package com.datacloud.platform;
import org.springframework.boot.CommandLineRunner;import org.springframework.context.annotation.*;
@Configuration public class SeedData {
 @Bean CommandLineRunner seed(UserRepository users,ProductRepository products){return args->{
  if(!users.existsById(1))users.save(new UserCoins(1,2000));
  if(!users.existsById(2))users.save(new UserCoins(2,1000));
  if(!products.existsById(10))products.save(new Product(10,"Producto de prueba",500,20));
 };}
}
