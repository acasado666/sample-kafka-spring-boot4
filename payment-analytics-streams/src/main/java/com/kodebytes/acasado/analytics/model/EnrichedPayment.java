package com.kodebytes.acasado.analytics.model;
import java.math.BigDecimal; import java.time.Instant; import java.util.List;
public record EnrichedPayment(String schemaVersion,String paymentId,String accountId,String merchantId,BigDecimal amount,String currency,String country,Instant occurredAt,int merchantRiskScore,boolean fraudulent,List<String> fraudReasons){}