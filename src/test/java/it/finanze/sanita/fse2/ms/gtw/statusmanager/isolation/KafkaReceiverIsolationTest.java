package it.finanze.sanita.fse2.ms.gtw.statusmanager.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl.KafkaReceiverSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(KafkaReceiverSRV.class) // No @EnableKafka: no listener containers or broker.
class KafkaReceiverIsolationTest {
    @Autowired KafkaReceiverSRV receiver;
    @MockitoBean ITransactionEventsSRV service;

    @Test
    void ignoresNullRecordAndForwardsRecordKeyAndValue() {
        receiver.listenerGtw(null, null);
        verifyNoInteractions(service);
        receiver.listenerGtw(new ConsumerRecord<>("events", 0, 10, "workflow", "{}"), null);
        verify(service).saveEvent("workflow", "{}");
    }

    @Test
    void forwardsNullKeysWithoutInventingWorkflow() {
        receiver.listenerGtw(new ConsumerRecord<>("events", 0, 10, null, "{}"), null);
        verify(service).saveEvent(null, "{}");
    }

    @Test
    void wrapsNestedFailureSoContainerCanApplyDeadLetterPolicy() {
        var failure = new IllegalStateException("save failed", new IllegalArgumentException("bad event"));
        doThrow(failure).when(service).saveEvent("workflow", "{}");
        assertSame(failure, assertThrows(BusinessException.class,
            () -> receiver.listenerGtw(new ConsumerRecord<>("events", 0, 10, "workflow", "{}"), null)).getCause());
    }
}
