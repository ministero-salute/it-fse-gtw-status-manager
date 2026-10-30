package it.finanze.sanita.fse2.ms.gtw.statusmanager.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.impl.TransactionStatusCTL;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({TransactionStatusCTL.class, ObjectMapper.class})
class ControllersIsolationTest {
    @Autowired TransactionStatusCTL controller;
    @MockitoBean ITransactionEventsSRV service;

    @Test
    void callbackSerializesExpectedEventAndOptionalMessage() {
        var request = TransactionServiceIsolationTest.request("UAR_FINAL_STATUS", "SUCCESS");
        assertTrue(controller.saveTransactionStatus(request).getSuccess());
        var captor = ArgumentCaptor.forClass(String.class);
        verify(service).saveEvent(eq("workflow"), captor.capture());
        Document event = Document.parse(captor.getValue());
        assertEquals("UAR_FINAL_STATUS", event.getString("eventType"));
        assertEquals("SUCCESS", event.getString("eventStatus"));
        assertEquals("message", event.getString("message"));
        assertTrue(event.getString("eventDate").matches("2023-11-.*[+-][0-9]{4}"));
        reset(service);
        request.setMessage(null);
        controller.saveTransactionStatus(request);
        verify(service).saveEvent(eq("workflow"), captor.capture());
        assertFalse(Document.parse(captor.getValue()).containsKey("message"));
    }

    @Test
    void preservesBusinessFailureAndWrapsUnexpectedFailure() {
        var request = TransactionServiceIsolationTest.request("type", "status");
        var business = new BusinessException("rejected");
        doThrow(business).when(service).saveEvent(anyString(), anyString());
        assertSame(business, assertThrows(BusinessException.class, () -> controller.saveTransactionStatus(request)));
        var failure = new IllegalStateException("offline");
        doThrow(failure).when(service).saveEvent(anyString(), anyString());
        assertSame(failure, assertThrows(BusinessException.class, () -> controller.saveTransactionStatus(request)).getCause());
    }

    @Test
    void missingDateFailsBeforeSaving() {
        var request = TransactionServiceIsolationTest.request("type", "status");
        request.setInsertionDate(null);
        assertThrows(BusinessException.class, () -> controller.saveTransactionStatus(request));
        verifyNoInteractions(service);
    }
}
