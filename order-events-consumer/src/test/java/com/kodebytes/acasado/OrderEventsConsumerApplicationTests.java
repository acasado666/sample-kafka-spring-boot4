package com.kodebytes.acasado;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
@EmbeddedKafka(partitions = 1)
class OrderEventsConsumerApplicationTests {

    @Test
    void contextLoads() {
    }

}
