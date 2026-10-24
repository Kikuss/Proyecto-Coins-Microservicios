package com.datacloud.worker;
public record FailureReport(Integer idPlatformOrigin, Integer idCoin, Integer idPlatformDestiny, Integer idProduct, Integer Mount, Integer Quantity, Integer idUser, String reason, Integer attempts) {
 public static FailureReport of(ExchangeRequest r, String reason, int attempts){
  return new FailureReport(r.idPlatformOrigin(),r.idCoin(),r.idPlatformDestiny(),r.idProduct(),r.Mount(),r.Quantity(),r.idUser(),reason,attempts);
 }
}
