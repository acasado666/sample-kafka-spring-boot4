package com.kodebytes.acasado.analytics.fraud;
import static org.assertj.core.api.Assertions.assertThat; import com.kodebytes.acasado.analytics.model.*; import java.math.BigDecimal; import java.time.Instant; import org.junit.jupiter.api.Test;
class FraudRulesEngineTest {
 private final FraudRulesEngine rules=new FraudRulesEngine();
 @Test void combinesRules(){var p=new PaymentEvent("1","p1","a1","m1",new BigDecimal("12000"),"CHF","CH",Instant.now()); var r=new MerchantRisk("m1",90,false,Instant.now()); assertThat(rules.evaluate(p,r)).containsExactly("HIGH_VALUE","HIGH_RISK_MERCHANT");}
 @Test void acceptsNormalPayment(){var p=new PaymentEvent("1","p2","a1","m2",new BigDecimal("50"),"CHF","CH",Instant.now()); assertThat(rules.evaluate(p,null)).isEmpty();}
}