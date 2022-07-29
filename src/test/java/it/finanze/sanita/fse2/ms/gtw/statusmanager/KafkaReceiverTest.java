package it.finanze.sanita.fse2.ms.gtw.statusmanager;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka.KafkaPropertiesCFG;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Description;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.IKafkaReceiverSRV;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class KafkaReceiverTest {

    @Autowired
    private IKafkaReceiverSRV kafkaReceiverSRV;

    @SpyBean
    private ITransactionEventsSRV eventsSRV;

    @Value("${kafka.statusmanager.topic}")
    String topic;

    @Test
    @Description("Generic error test on status manager listener - wrong json")
    void kafkaReceiverErrorTest() {
        Map<String, Object> map = new HashMap<>();
        MessageHeaders headers = new MessageHeaders(map);

        Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();

        records.put(new TopicPartition(topic, 0), new ArrayList<>());
        ConsumerRecord<String, String> errorRecord =
                new ConsumerRecord<>(topic, 0, 0, "key", "value");
        assertThrows(BusinessException.class, ()->kafkaReceiverSRV.listener(errorRecord, headers));
    }

    @Test
    @Description("Success test on status manager listener - ok")
    void kafkaReceiverSuccessTest() {
        Map<String, Object> map = new HashMap<>();
        MessageHeaders headers = new MessageHeaders(map);

        Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new LinkedHashMap<>();

        records.put(new TopicPartition(topic, 0), new ArrayList<>());
        ConsumerRecord<String, String> successRecord =
                new ConsumerRecord<>(topic, 0, 0, "key", "{\"eventDate\":\"2022-07-29T11:33:35.316-0500\"}");

        assertDoesNotThrow(()->kafkaReceiverSRV.listener(successRecord, headers));
    }
}
