package com.kodebytes.acasado.analytics.fraud;
import com.kodebytes.acasado.analytics.model.*; import java.math.BigDecimal; import java.util.*; import org.springframework.stereotype.Component;
@Component public class FraudRulesEngine {
 private static final BigDecimal HIGH_VALUE=new BigDecimal("10000.00"); private static final int HIGH_RISK=80;
 public List<String> evaluate(PaymentEvent p,MerchantRisk r){List<String>x=new ArrayList<>(); if(p.amount().compareTo(HIGH_VALUE)>=0)x.add("HIGH_VALUE"); if(r!=null&&r.blocked())x.add("BLOCKED_MERCHANT"); if(r!=null&&r.riskScore()>=HIGH_RISK)x.add("HIGH_RISK_MERCHANT"); return List.copyOf(x);}
}