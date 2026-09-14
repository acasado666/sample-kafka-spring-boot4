package com.kodebytes.acasado.analytics;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.kafka.annotation.EnableKafkaStreams;
@SpringBootApplication @ConfigurationPropertiesScan @EnableKafkaStreams
public class PaymentAnalyticsApplication {
 public static void main(String[] args){SpringApplication.run(PaymentAnalyticsApplication.class,args);}
}