package com.kodebytes.acasado.analytics.model;
import java.math.BigDecimal; import java.time.Instant;
public record AccountPaymentMetric(String accountId,long paymentCount,BigDecimal totalAmount,long fraudCount,Instant windowStart,Instant windowEnd){
 public static AccountPaymentMetric empty(){return new AccountPaymentMetric("",0,BigDecimal.ZERO,0,Instant.EPOCH,Instant.EPOCH);}
 public AccountPaymentMetric add(EnrichedPayment p){return new AccountPaymentMetric(p.accountId(),paymentCount+1,totalAmount.add(p.amount()),fraudCount+(p.fraudulent()?1:0),windowStart,windowEnd);}
 public AccountPaymentMetric withWindow(Instant start,Instant end){return new AccountPaymentMetric(accountId,paymentCount,totalAmount,fraudCount,start,end);}
}