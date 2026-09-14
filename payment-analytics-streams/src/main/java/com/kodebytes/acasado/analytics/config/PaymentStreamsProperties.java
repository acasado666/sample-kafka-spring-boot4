package com.kodebytes.acasado.analytics.config;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("app.kafka")
public record PaymentStreamsProperties(String paymentsTopic,String merchantRisksTopic,String enrichedPaymentsTopic,String fraudAlertsTopic,String accountMetricsTopic,Duration metricsWindow,Duration gracePeriod){}