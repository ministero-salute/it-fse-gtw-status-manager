package it.finanze.sanita.fse2.ms.gtw.statusmanager.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka.KafkaTopicCFG;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.CallbackTransactionDataRequestDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionDataETY;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl.TransactionEventsSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(TransactionEventsSRV.class)
class TransactionServiceIsolationTest {
    @Autowired TransactionEventsSRV service;
    @MockitoBean ITransactionEventsRepo repository;
    @MockitoBean(name = "notxkafkatemplate") KafkaTemplate<String, String> kafka;
    @MockitoBean KafkaTopicCFG topics;

    @ParameterizedTest
    @CsvSource({"SEND_TO_INI,BLOCKING_ERROR,true", "SEND_TO_UAR,BLOCKING_ERROR,true",
        "UAR_FINAL_STATUS,SUCCESS,true", "UAR_FINAL_STATUS,BLOCKING_ERROR,true",
        "BROKER_COMMUNICATION_ERROR,BLOCKING_ERROR,true", "SEND_TO_UAR,SUCCESS,false",
        "OTHER,BLOCKING_ERROR,false", "UAR_FINAL_STATUS,PENDING,false", ",SUCCESS,false", "SEND_TO_INI,,false"})
    void notifiesOnlyFinalJsonEvents(String type, String status, boolean notify) {
        when(topics.getFinalStateTopic()).thenReturn("final-status");
        String json = new org.bson.Document("eventType", type).append("eventStatus", status).toJson();
        service.saveEvent("workflow", json);
        verify(repository).saveEvent("workflow", json);
        if (notify) verify(kafka).send("final-status", "workflow", "workflow");
        else verifyNoInteractions(kafka);
    }

    @Test
    void kafkaFailureDoesNotUndoSavedJsonEvent() {
        when(topics.getFinalStateTopic()).thenReturn("final-status");
        when(kafka.send(anyString(), anyString(), anyString())).thenThrow(new IllegalStateException("offline"));
        assertDoesNotThrow(() -> service.saveEvent("workflow", "{eventType:'UAR_FINAL_STATUS',eventStatus:'SUCCESS'}"));
        verify(repository).saveEvent(eq("workflow"), anyString());
    }

    @Test
    void malformedJsonAndPersistenceFailureAreWrapped() {
        assertThrows(BusinessException.class, () -> service.saveEvent("workflow", "not json"));
        verifyNoInteractions(repository, kafka);
        RuntimeException failure = new IllegalStateException("database offline");
        doThrow(failure).when(repository).saveEvent("workflow", "{}");
        assertSame(failure, assertThrows(BusinessException.class, () -> service.saveEvent("workflow", "{}")).getCause());
    }

    @ParameterizedTest
    @CsvSource({"UAR_FINAL_STATUS,SUCCESS,true", "SEND_TO_UAR,SUCCESS,false"})
    void savesTypedStatusAndUsesPersistedIdForNotification(String type, String status, boolean notify) {
        var request = request(type, status);
        TransactionDataETY saved = new TransactionDataETY();
        saved.setId("persisted-id"); saved.setWorkflowInstanceId("workflow"); saved.setType(type); saved.setStatus(status);
        when(repository.saveEvent("workflow", request.getInsertionDate(), type, status, null, null, null, "message")).thenReturn(saved);
        when(topics.getFinalStateTopic()).thenReturn("final-status");
        assertTrue(service.saveTransactionStatus(request).getSuccess());
        if (notify) verify(kafka).send("final-status", "persisted-id", "workflow");
        else verifyNoInteractions(kafka);
    }

    @Test
    void typedNotificationIsBestEffortAndPersistenceFailureIsNot() {
        var request = request("UAR_FINAL_STATUS", "SUCCESS");
        TransactionDataETY saved = new TransactionDataETY();
        saved.setType(request.getType()); saved.setStatus(request.getStatus());
        when(repository.saveEvent(anyString(), any(), anyString(), anyString(), isNull(), isNull(), isNull(), anyString())).thenReturn(saved);
        when(kafka.send(nullable(String.class), nullable(String.class), nullable(String.class))).thenThrow(new IllegalStateException("offline"));
        assertTrue(service.saveTransactionStatus(request).getSuccess());
        reset(repository);
        RuntimeException failure = new IllegalStateException("insert failed");
        when(repository.saveEvent(anyString(), any(), anyString(), anyString(), isNull(), isNull(), isNull(), anyString())).thenThrow(failure);
        assertSame(failure, assertThrows(BusinessException.class, () -> service.saveTransactionStatus(request)).getCause());
    }

    @Test
    void edsCallbackUsesFinalTypeAndWrapsFailure() {
        var request = request("ignored", "SUCCESS");
        service.saveEvent(request);
        verify(repository).saveEdsEvent("workflow", request.getInsertionDate(), "UAR_FINAL_STATUS", "SUCCESS", "message");
        RuntimeException failure = new IllegalStateException("save failed");
        when(repository.saveEdsEvent(anyString(), any(), anyString(), anyString(), anyString())).thenThrow(failure);
        assertSame(failure, assertThrows(BusinessException.class, () -> service.saveEvent(request)).getCause());
    }

    static CallbackTransactionDataRequestDTO request(String type, String status) {
        return CallbackTransactionDataRequestDTO.builder().workflowInstanceId("workflow").insertionDate(new Date(1700000000000L))
            .type(type).status(status).message("message").build();
    }
}
