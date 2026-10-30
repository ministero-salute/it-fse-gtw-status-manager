package it.finanze.sanita.fse2.ms.gtw.statusmanager.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.ResourceAccessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IEdsClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.client.eds.GetIngestionStatusResDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionDataETY;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.ITransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.executors.impl.EdsStatusCheckExecutor;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({EdsStatusCheckExecutor.class, ObjectMapper.class})
@TestPropertySource(properties = "scheduler.eds-status-check.event-age-threshold-minutes=15")
class SchedulerIsolationTest {
    @Autowired EdsStatusCheckExecutor scheduler;
    @MockitoBean IEdsClient eds;
    @MockitoBean ITransactionEventsRepo repository;
    @MockitoBean ITransactionEventsSRV service;

    @Test
    void emptyBatchUsesConfiguredThresholdWithoutCallingEds() {
        Instant before = Instant.now().minusSeconds(15 * 60);
        scheduler.run();
        var date = ArgumentCaptor.forClass(Date.class);
        verify(repository).findPendingUarTransactions(date.capture(), eq(1000));
        assertFalse(date.getValue().toInstant().isBefore(before.minusMillis(1)));
        assertFalse(date.getValue().toInstant().isAfter(Instant.now().minusSeconds(15 * 60)));
        verifyNoInteractions(eds, service);
    }

    @Test
    void persistsFinalStatusForEachTransactionAndOmitsAbsentMessage() {
        pending("one", "two");
        when(eds.getIngestionStatus("one")).thenReturn(status("done"));
        when(eds.getIngestionStatus("two")).thenReturn(status(null));
        scheduler.run();
        var json = ArgumentCaptor.forClass(String.class);
        verify(service).saveEvent(eq("one"), json.capture());
        Document event = Document.parse(json.getValue());
        assertEquals("UAR_FINAL_STATUS", event.getString("eventType"));
        assertEquals("SUCCESS", event.getString("eventStatus"));
        assertEquals("done", event.getString("message")); assertNotNull(event.getString("eventDate"));
        verify(service).saveEvent(eq("two"), json.capture());
        assertFalse(Document.parse(json.getValue()).containsKey("message"));
        verify(repository, never()).updatePullStatusOutcome(anyString(), anyString());
    }

    @Test
    void businessErrorsAreNotRetriedAndDoNotPreventFollowingTransactions() {
        pending("one", "two");
        when(eds.getIngestionStatus("one")).thenThrow(new BusinessException("invalid"));
        when(eds.getIngestionStatus("two")).thenReturn(status("done"));
        when(repository.updatePullStatusOutcome("one", TransactionDataETY.PULL_STATUS_BLOCKED)).thenReturn(true);
        scheduler.run();
        verify(eds).getIngestionStatus("one");
        verify(repository).updatePullStatusOutcome("one", TransactionDataETY.PULL_STATUS_BLOCKED);
        verify(service).saveEvent(eq("two"), anyString());
    }

    @Test
    void transientNetworkErrorRecoversOnSecondAttempt() {
        pending("one");
        when(eds.getIngestionStatus("one")).thenThrow(new ResourceAccessException("offline")).thenReturn(status("done"));
        scheduler.run();
        verify(eds, times(2)).getIngestionStatus("one");
        verify(service).saveEvent(eq("one"), anyString());
        verify(repository, never()).updatePullStatusOutcome(anyString(), anyString());
    }

    @Test
    void exhaustedNullResponsesBlockTransaction() {
        pending("one");
        scheduler.run();
        verify(eds, times(3)).getIngestionStatus("one");
        verify(repository).updatePullStatusOutcome("one", TransactionDataETY.PULL_STATUS_BLOCKED);
        verifyNoInteractions(service);
    }

    @Test
    void exhaustedCommunicationErrorsBlockTransactionEvenIfUpdateFails() {
        pending("one");
        when(eds.getIngestionStatus("one")).thenThrow(new ResourceAccessException("offline"));
        when(repository.updatePullStatusOutcome("one", TransactionDataETY.PULL_STATUS_BLOCKED)).thenThrow(new IllegalStateException("db offline"));
        assertDoesNotThrow(scheduler::run);
        verify(eds, times(3)).getIngestionStatus("one");
        verify(repository).updatePullStatusOutcome("one", TransactionDataETY.PULL_STATUS_BLOCKED);
        verifyNoInteractions(service);
    }

    @Test
    void unexpectedSaveFailureBlocksTransactionAndFatalQueryFailureIsContained() {
        pending("one");
        when(eds.getIngestionStatus("one")).thenReturn(status("done"));
        doThrow(new IllegalStateException("save failed")).when(service).saveEvent(anyString(), anyString());
        scheduler.run();
        verify(repository).updatePullStatusOutcome("one", TransactionDataETY.PULL_STATUS_BLOCKED);
        when(repository.findPendingUarTransactions(any(), anyInt())).thenThrow(new IllegalStateException("db offline"));
        assertDoesNotThrow(scheduler::run);
    }

    private void pending(String... ids) {
        when(repository.findPendingUarTransactions(any(), eq(1000))).thenReturn(java.util.Arrays.stream(ids).map(id -> {
            var event = new TransactionDataETY(); event.setWorkflowInstanceId(id); return event;
        }).toList());
    }

    private GetIngestionStatusResDTO status(String message) {
        return GetIngestionStatusResDTO.builder().status("SUCCESS").message(message).build();
    }
}
