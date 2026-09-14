package com.kodebytes.acasado.analytics.model;
import java.math.BigDecimal; import java.time.Instant;
public record PaymentEvent(String schemaVersion,String paymentId,String accountId,String merchantId,BigDecimal amount,String currency,String country,Instant occurredAt){}