package com.kodebytes.acasado.analytics.config;
import com.kodebytes.acasado.analytics.fraud.FraudRulesEngine; import com.kodebytes.acasado.analytics.model.*;
import java.time.Instant; import java.util.List;
import org.apache.kafka.common.serialization.Serdes; import org.apache.kafka.common.utils.Bytes; import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*; import org.apache.kafka.streams.state.WindowStore;
import org.springframework.context.annotation.*; import org.springframework.kafka.support.serializer.JsonSerde;
@Configuration(proxyBeanMethods=false)
public class PaymentAnalyticsTopology {
 public static final String METRICS_STORE="account-payment-metrics";
 @Bean KStream<String,EnrichedPayment> paymentStream(StreamsBuilder b,PaymentStreamsProperties p,FraudRulesEngine rules){
  JsonSerde<PaymentEvent> ps=new JsonSerde<>(PaymentEvent.class); JsonSerde<MerchantRisk> rs=new JsonSerde<>(MerchantRisk.class);
  JsonSerde<EnrichedPayment> es=new JsonSerde<>(EnrichedPayment.class); JsonSerde<AccountPaymentMetric> ms=new JsonSerde<>(AccountPaymentMetric.class);
  KTable<String,MerchantRisk> risks=b.table(p.merchantRisksTopic(),Consumed.with(Serdes.String(),rs));
  KStream<String,PaymentEvent> input=b.stream(p.paymentsTopic(),Consumed.with(Serdes.String(),ps))
   .filter((k,v)->k!=null&&v!=null&&v.accountId()!=null&&v.amount()!=null);
  KStream<String,EnrichedPayment> enriched=input.leftJoin(risks,(payment,risk)->enrich(payment,risk,rules),Joined.with(Serdes.String(),ps,rs));
  enriched.to(p.enrichedPaymentsTopic(),Produced.with(Serdes.String(),es));
  enriched.filter((k,v)->v.fraudulent()).to(p.fraudAlertsTopic(),Produced.with(Serdes.String(),es));
  enriched.selectKey((k,v)->v.accountId()).groupByKey(Grouped.with(Serdes.String(),es))
   .windowedBy(TimeWindows.ofSizeAndGrace(p.metricsWindow(),p.gracePeriod()))
   .aggregate(AccountPaymentMetric::empty,(account,payment,total)->total.add(payment),
    Materialized.<String,AccountPaymentMetric,WindowStore<Bytes,byte[]>>as(METRICS_STORE).withKeySerde(Serdes.String()).withValueSerde(ms))
   .toStream().map((key,value)->KeyValue.pair(key.key()+"@"+key.window().start(),
    value.withWindow(Instant.ofEpochMilli(key.window().start()),Instant.ofEpochMilli(key.window().end()))))
   .to(p.accountMetricsTopic(),Produced.with(Serdes.String(),ms));
  return enriched;
 }
 private EnrichedPayment enrich(PaymentEvent p,MerchantRisk r,FraudRulesEngine rules){List<String>x=rules.evaluate(p,r); return new EnrichedPayment(p.schemaVersion(),p.paymentId(),p.accountId(),p.merchantId(),p.amount(),p.currency(),p.country(),p.occurredAt(),r==null?0:r.riskScore(),!x.isEmpty(),x);}
}