package com.kodebytes.acasado.analytics.model;
import java.time.Instant;
public record MerchantRisk(String merchantId,int riskScore,boolean blocked,Instant updatedAt){}