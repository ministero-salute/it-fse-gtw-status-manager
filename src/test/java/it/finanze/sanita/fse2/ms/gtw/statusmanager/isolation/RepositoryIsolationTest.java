package it.finanze.sanita.fse2.ms.gtw.statusmanager.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants.Fields.*;
import java.util.Date;
import java.util.List;
import com.mongodb.MongoException;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.OperationException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionDataETY;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.mongo.impl.TransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.IConfigSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig(TransactionEventsRepo.class)
class RepositoryIsolationTest {
    @Autowired TransactionEventsRepo repository;
    @MockitoBean MongoTemplate mongo;
    @MockitoBean IConfigSRV config;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void jsonUpsertUsesWorkflowOrTraceAndAppliesPrivacyPolicy(boolean unknown) {
        when(config.getExpirationDate()).thenReturn(3);
        when(config.isCfOnIssuerNotAllowed()).thenReturn(unknown);
        when(config.isSubjectNotAllowed()).thenReturn(unknown);
        String workflow = unknown ? "UNKNOWN_WORKFLOW_ID" : "workflow";
        String json = new Document(EVENT_DATE, "2023-11-14T22:13:20.000+0000").append(EVENT_TYPE, "SEND_TO_UAR")
            .append(EVENT_STATUS, "SUCCESS").append(TRACE_ID, "trace").append(EVENT_ISSUER, "organization#taxcode")
            .append(EVENT_SUBJECT, "private-subject").toJson();
        long before = System.currentTimeMillis();
        repository.saveEvent(workflow, json);
        var query = ArgumentCaptor.forClass(Query.class);
        var update = ArgumentCaptor.forClass(Update.class);
        verify(mongo).upsert(query.capture(), update.capture(), eq(TransactionDataETY.class));
        assertEquals(unknown ? "trace" : workflow, query.getValue().getQueryObject().get(unknown ? TRACE_ID : WORKFLOW_INSTANCE_ID));
        assertEquals("SEND_TO_UAR", query.getValue().getQueryObject().get(EVENT_TYPE));
        assertEquals("SUCCESS", query.getValue().getQueryObject().get(EVENT_STATUS));
        Document saved = update.getValue().getUpdateObject();
        assertEquals(workflow, saved.getString(WORKFLOW_INSTANCE_ID));
        assertEquals(new Date(1700000000000L), saved.getDate(EVENT_DATE));
        assertEquals(unknown ? "organization" : "organization#taxcode", saved.getString(EVENT_ISSUER));
        assertEquals(!unknown, saved.containsKey(EVENT_SUBJECT));
        assertTrue(saved.getDate(EXPIRING_DATE).getTime() > before + 2 * 86400000L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void typedInsertHonorsPrivacyAndOptionalFields(boolean clean) {
        when(config.getExpirationDate()).thenReturn(1);
        when(config.isCfOnIssuerNotAllowed()).thenReturn(clean);
        when(config.isSubjectNotAllowed()).thenReturn(clean);
        when(mongo.insert(any(TransactionDataETY.class))).thenAnswer(inv -> inv.getArgument(0));
        Date date = new Date();
        var saved = repository.saveEvent("workflow", date, "type", "status", "trace", "org#cf", "subject", "message");
        assertEquals("workflow", saved.getWorkflowInstanceId());
        assertEquals(date, saved.getDate());
        assertEquals("trace", saved.getTraceId());
        assertEquals(clean ? "org" : "org#cf", saved.getIssuer());
        assertEquals(clean ? null : "subject", saved.getSubject());
        assertEquals("message", saved.getMessage());
        assertNotNull(saved.getExpiringDate());
        saved = repository.saveEvent("workflow", date, "type", "status", null, null, null, null);
        assertNull(saved.getTraceId()); assertNull(saved.getIssuer()); assertNull(saved.getSubject()); assertNull(saved.getMessage());
        if (clean) assertEquals("org", repository.saveEvent("workflow", date, "type", "status", null, "org", null, null).getIssuer());
    }

    @Test
    void wrapsParseAndInsertFailures() {
        assertThrows(BusinessException.class, () -> repository.saveEvent("workflow", "{}"));
        when(mongo.insert(any(TransactionDataETY.class))).thenThrow(new MongoException("offline"));
        assertInstanceOf(MongoException.class, assertThrows(BusinessException.class,
            () -> repository.saveEvent("workflow", new Date(), "type", "status", null, null, null, null)).getCause());
    }

    @Test
    void edsEventPersistsOptionalMessage() {
        when(mongo.save(any(TransactionDataETY.class))).thenAnswer(inv -> inv.getArgument(0));
        var event = repository.saveEdsEvent("workflow", new Date(0), "UAR_FINAL_STATUS", "SUCCESS", "done");
        assertEquals("workflow", event.getWorkflowInstanceId()); assertEquals("done", event.getMessage());
        assertEquals("SUCCESS", event.getStatus()); assertEquals("UAR_FINAL_STATUS", event.getType());
        assertNull(repository.saveEdsEvent("workflow", new Date(), "type", "status", null).getMessage());
    }

    @Test
    void bulkFhirEventsHaveSuccessStatusAndWrapMongoFailure() throws Exception {
        when(mongo.insertAll(anyCollection())).thenAnswer(inv -> inv.getArgument(0));
        Date timestamp = new Date(0), expiry = new Date(86400000L);
        assertEquals(2, repository.saveEventsFhir(List.of("one", "two"), timestamp, expiry));
        var captor = ArgumentCaptor.forClass(java.util.Collection.class);
        verify(mongo).insertAll(captor.capture());
        for (Object item : captor.getValue()) {
            var event = (TransactionDataETY) item;
            assertEquals("SUCCESS", event.getStatus()); assertEquals(timestamp, event.getDate()); assertEquals(expiry, event.getExpiringDate());
        }
        when(mongo.insertAll(anyCollection())).thenThrow(new MongoException("offline"));
        assertThrows(OperationException.class, () -> repository.saveEventsFhir(List.of("one"), timestamp, expiry));
    }

    @Test
    void pendingAggregationFiltersFinalAndBlockedEventsAndLimitsBatch() {
        var event = new TransactionDataETY();
        when(mongo.aggregate(any(Aggregation.class), eq(TransactionDataETY.class), eq(TransactionDataETY.class)))
            .thenReturn(new AggregationResults<>(List.of(event), new Document()));
        assertEquals(List.of(event), repository.findPendingUarTransactions(new Date(0), 25));
        var captor = ArgumentCaptor.forClass(Aggregation.class);
        verify(mongo).aggregate(captor.capture(), eq(TransactionDataETY.class), eq(TransactionDataETY.class));
        var pipeline = captor.getValue().toPipeline(Aggregation.DEFAULT_CONTEXT);
        assertEquals(5, pipeline.size());
        assertEquals(25L, ((Number) pipeline.get(4).get("$limit")).longValue());
        assertTrue(pipeline.get(0).toJson().contains(TransactionDataETY.PULL_STATUS_BLOCKED));
        assertTrue(pipeline.get(2).toJson().contains("UAR_FINAL_STATUS"));
        assertTrue(pipeline.get(2).toJson().contains("BROKER_COMMUNICATION_ERROR"));
        assertTrue(captor.getValue().getOptions().isAllowDiskUse());
    }

    @Test
    void updateOutcomeReturnsActualModificationAndContainsDatabaseFailures() {
        when(mongo.updateFirst(any(Query.class), any(Update.class), eq(TransactionDataETY.class)))
            .thenReturn(UpdateResult.acknowledged(1, 1L, null), UpdateResult.acknowledged(0, 0L, null))
            .thenThrow(new MongoException("offline"));
        assertTrue(repository.updatePullStatusOutcome("workflow", "blocked"));
        var query = ArgumentCaptor.forClass(Query.class); var update = ArgumentCaptor.forClass(Update.class);
        verify(mongo).updateFirst(query.capture(), update.capture(), eq(TransactionDataETY.class));
        assertEquals("workflow", query.getValue().getQueryObject().get(TransactionDataETY.FIELD_WIF));
        assertEquals("SEND_TO_UAR", query.getValue().getQueryObject().get(TransactionDataETY.FIELD_EVENT_TYPE));
        assertEquals("blocked", ((Document) update.getValue().getUpdateObject().get("$set")).get(TransactionDataETY.FIELD_PULL_STATUS_OUTCOME));
        assertFalse(repository.updatePullStatusOutcome("workflow", "blocked"));
        assertFalse(repository.updatePullStatusOutcome("workflow", "blocked"));
    }
}
