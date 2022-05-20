package it.finanze.sanita.fse2.ms.gtw.statusmanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.IErrorTransactionInspectionCTL;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.ITransactionInspectionCTL;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.TransactionEventDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response.TransactionDetailResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response.TransactionsResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.EntityNotFoundException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionEventsETY;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.impl.TransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.StringUtility;

/**
 * Test Transaction Inspection APIs
 */
@SpringBootTest
@ActiveProfiles(Constants.Profile.TEST)
class TransactionInspectionTest {

    @Autowired
    private TransactionEventsRepo validationEventsRepo;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ITransactionInspectionCTL transactionInspectionCTL;

    @Autowired
    private IErrorTransactionInspectionCTL errorTransactionInspectionCTL;

    @BeforeEach
    void setup() {
        mongoTemplate.dropCollection(TransactionEventsETY.class);
    }

    @Test
    @DisplayName("Retrieve full transaction details by Transaction ID (txID)")
    void transactionDetail() throws ParseException {

        final String transactionID = StringUtility.generateUUID();
        generateTransactionEvent(transactionID);


        TransactionDetailResponseDTO response = transactionInspectionCTL.getTransactionDetail(transactionID, null);

        assertNotNull(response);
        assertNotNull(response.getTransactionInfo());
        assertEquals(EventTypeEnum.PUBLICATION, response.getTransactionInfo().getLastEventType());
        assertEquals(ValidationResultEnum.OK, response.getTransactionInfo().getLastValidationResult());
        assertEquals(PublicationResultEnum.OK, response.getTransactionInfo().getLastPublicationResult());

    }



    @Test
    @DisplayName("Retrieve transaction details error by Transaction ID (txID)")
    void errorTransactionDetail() throws ParseException {

        final String transactionID = StringUtility.generateUUID();
        generateErrorTransactionEvent(transactionID);


        TransactionDetailResponseDTO response = errorTransactionInspectionCTL.getTransactionDetail(transactionID, null);

        assertNotNull(response);
        assertNotNull(response.getTransactionInfo());
        assertEquals(EventTypeEnum.PUBLICATION, response.getTransactionInfo().getLastEventType());
        assertEquals(ValidationResultEnum.OK, response.getTransactionInfo().getLastValidationResult());
        assertEquals(PublicationResultEnum.FHIR_MAPPING_ERROR, response.getTransactionInfo().getLastPublicationResult());
        assertEquals(1, response.getTransactionInfo().getEvents().size());


        final String transactionID_1 = StringUtility.generateUUID();
        generateTransactionEvent(transactionID_1);

        assertThrows(EntityNotFoundException.class, () -> errorTransactionInspectionCTL.getTransactionDetail(transactionID_1, null));
    }

    @Test
    @DisplayName("Find transaction using the provided filters")
    void findTransaction() throws ParseException {

        generateTransactionEvent(StringUtility.generateUUID());
        generateErrorTransactionEvent(StringUtility.generateUUID());
        generateTransactionEventsWithErrors();

        TransactionsResponseDTO response = transactionInspectionCTL.findTransaction(null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertNotNull(response);
        List<TransactionEventsETY> allRecords = mongoTemplate.findAll(TransactionEventsETY.class);
        assertEquals(allRecords.size(), response.getTransactions().size(), "The find request with null parameters should return all the transaction events on db");

        response = transactionInspectionCTL.findTransaction(ValidationResultEnum.OK, null, null, null, null, null, null, null, null, null, null, null, null);
        assertEquals(2, response.getTransactions().size(), "The find request should return only 1 record");

        response = transactionInspectionCTL.findTransaction(null, PublicationResultEnum.FHIR_MAPPING_ERROR, null, null, null, null, null, null, null, null, null, null, null);
        assertEquals(1, response.getTransactions().size(), "The find request should return only 1 record");
        assertEquals(2, response.getTransactions().get(0).getEvents().size(), "The events array size should be 2");

        response = transactionInspectionCTL.findTransaction(null, null, null, null, ValidationResultEnum.OK, null, null, null, null, null, null, null, null);
        assertEquals(2, response.getTransactions().size(), "The find request should return 2 record");

        response = transactionInspectionCTL.findTransaction(null, null, null, null, null, null, "doc-identifier-01", null, null, null, null, null, null);
        assertEquals(1, response.getTransactions().size(), "The find request should return only 1 record");
        assertEquals(2, response.getTransactions().get(0).getEvents().size(), "The events array size should be 2");

        response = transactionInspectionCTL.findTransaction(null, null, null, null, null, null, null, "paziente-identifier-01", null, null, null, null, null);
        assertEquals(1, response.getTransactions().size(), "The find request should return only 1 record");
        assertEquals(2, response.getTransactions().get(0).getEvents().size(), "The events array size should be 2");

        response = transactionInspectionCTL.findTransaction(null, null, null, null, null, null, null, null, "submission-identifier-01", null, null, null, null);
        assertEquals(1, response.getTransactions().size(), "The find request should return only 1 record");
        assertEquals(2, response.getTransactions().get(0).getEvents().size(), "The events array size should be 2");

        response = transactionInspectionCTL.findTransaction(null, null, null, null, null, null, null, null, null, null, "2022-04-18", null, null);
        assertEquals(1, response.getTransactions().size(), "The find request should return only 1 record");

        response = transactionInspectionCTL.findTransaction(null, null, null, null, null, null, null, null, null, null, "2022-04-15", "2022-04-18", null);
        assertEquals(3, response.getTransactions().size(), "The find request should return 3 records");

        response = transactionInspectionCTL.findTransaction(ValidationResultEnum.SYNTAX_ERROR, null, null, null, null, null, null, null, null, null, "2022-04-15", "2022-04-18", null);
        assertEquals(1, response.getTransactions().size(), "The find request should return 3 records");


    }

    @Test
    @DisplayName("Find transaction with error event using the provided filters")
    void findErrorTransaction() throws ParseException {

        generateTransactionEvent(StringUtility.generateUUID());
        generateErrorTransactionEvent(StringUtility.generateUUID());
        generateTransactionEventsWithErrors();

        TransactionsResponseDTO response = errorTransactionInspectionCTL.findErrorTransaction(null, null, null, null, null, null, null, null, null);
        assertNotNull(response);
        assertEquals(3, response.getTransactions().size(),  "The find request with null parameters should return only the transaction events with an error state as lastValidationResult or lastPublicationresult");

        response = errorTransactionInspectionCTL.findErrorTransaction(EventTypeEnum.PUBLICATION, null, null, null, null, null, null, null, null);
        assertEquals(3, response.getTransactions().size(),  "The find request should return 3 records");

        response = errorTransactionInspectionCTL.findErrorTransaction(null, null, "doc-identifier-01", null, null, null, null, null, null);
        assertEquals(1, response.getTransactions().size(),  "The find request should only return 1 record");
        assertEquals(1, response.getTransactions().get(0).getEvents().size(), "The events array size should be 1");
                

    }


    private void generateTransactionEvent(String transactionID) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date eventDate = sdf.parse("2022-04-15 12:00:00");
        Date eventDate1 = sdf.parse("2022-04-15 13:00:00");
        
        TransactionEventDTO event1 = TransactionEventDTO.builder().date(eventDate).eventType(EventTypeEnum.VALIDATION)
                .identificativoDoc(StringUtility.generateUUID())
                .identificativoPaziente(StringUtility.generateUUID())
                .identificativoSottomissione(StringUtility.generateUUID())
                .validationResult(ValidationResultEnum.OK).build();

        TransactionEventDTO event2 = TransactionEventDTO.builder().date(eventDate1).eventType(EventTypeEnum.PUBLICATION)
        .identificativoDoc(StringUtility.generateUUID())
        .identificativoPaziente(StringUtility.generateUUID())
        .identificativoSottomissione(StringUtility.generateUUID())
        .forcePublish(false).publicationResult(PublicationResultEnum.OK).build();
        
        List<TransactionEventDTO> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);

        TransactionEventsETY transactionEvent = new TransactionEventsETY();
        transactionEvent = new TransactionEventsETY();
        transactionEvent.setTransactionID(transactionID);
        transactionEvent.setLastUpdate(eventDate);
        transactionEvent.setLastEventType(EventTypeEnum.PUBLICATION);
        transactionEvent.setLastValidationResult(ValidationResultEnum.OK);
        transactionEvent.setLastPublicationResult(PublicationResultEnum.OK);
        
        transactionEvent.setEvents(events);

        validationEventsRepo.insert(transactionEvent);

    }

    private void generateErrorTransactionEvent(String transactionID) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date eventDate = sdf.parse("2022-04-15 12:00:00");
        Date eventDate1 = sdf.parse("2022-04-15 13:00:00");
        
        TransactionEventDTO event1 = TransactionEventDTO.builder().date(eventDate).eventType(EventTypeEnum.VALIDATION)
                .identificativoDoc(StringUtility.generateUUID())
                .identificativoPaziente(StringUtility.generateUUID())
                .identificativoSottomissione(StringUtility.generateUUID())
                .validationResult(ValidationResultEnum.OK).build();

        TransactionEventDTO event2 = TransactionEventDTO.builder().date(eventDate1).eventType(EventTypeEnum.PUBLICATION)
        .identificativoDoc(StringUtility.generateUUID())
        .identificativoPaziente(StringUtility.generateUUID())
        .identificativoSottomissione(StringUtility.generateUUID())
        .forcePublish(false).publicationResult(PublicationResultEnum.FHIR_MAPPING_ERROR).build();
        
        List<TransactionEventDTO> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);

        TransactionEventsETY transactionEvent = new TransactionEventsETY();
        transactionEvent = new TransactionEventsETY();
        transactionEvent.setTransactionID(transactionID);
        transactionEvent.setLastUpdate(eventDate);
        transactionEvent.setLastEventType(EventTypeEnum.PUBLICATION);
        transactionEvent.setLastValidationResult(ValidationResultEnum.OK);
        transactionEvent.setLastPublicationResult(PublicationResultEnum.FHIR_MAPPING_ERROR);
        
        transactionEvent.setEvents(events);

        validationEventsRepo.insert(transactionEvent);

    }

    private void generateTransactionEventsWithErrors() throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date eventDate = sdf.parse("2022-04-15 10:00:00");
        Date eventDate1 = sdf.parse("2022-04-16 10:00:00");

        Date eventDate2 = sdf.parse("2022-04-20 10:00:00");
        Date eventDate3 = sdf.parse("2022-04-20 18:00:00");

        // ----- Transaction with 2 events (1 OK, 1 Error)

        String transactionID = StringUtility.generateUUID();

        TransactionEventDTO event1 = TransactionEventDTO.builder().date(eventDate).eventType(EventTypeEnum.VALIDATION)
                .identificativoDoc("doc-identifier-01")
                .identificativoPaziente(StringUtility.generateUUID())
                .identificativoSottomissione(StringUtility.generateUUID())
                .validationResult(ValidationResultEnum.SYNTAX_ERROR).build();

        TransactionEventDTO event2 = TransactionEventDTO.builder().date(eventDate1).eventType(EventTypeEnum.PUBLICATION)
        .identificativoDoc("doc-identifier-01")
        .identificativoPaziente(StringUtility.generateUUID())
        .identificativoSottomissione(StringUtility.generateUUID())
        .forcePublish(true).publicationResult(PublicationResultEnum.OK_FORCED).build();
        
        
        List<TransactionEventDTO> events = new ArrayList<>();
        events.add(event1);
        events.add(event2);

        TransactionEventsETY transactionEvent = new TransactionEventsETY();
        transactionEvent = new TransactionEventsETY();
        transactionEvent.setTransactionID(transactionID);
        transactionEvent.setLastUpdate(eventDate1);
        transactionEvent.setLastEventType(EventTypeEnum.PUBLICATION);
        transactionEvent.setLastValidationResult(ValidationResultEnum.SYNTAX_ERROR);
        transactionEvent.setLastPublicationResult(PublicationResultEnum.OK_FORCED);
        transactionEvent.setEvents(events);

        validationEventsRepo.insert(transactionEvent);


        // ----- Transaction with 2 events (2 Error)
        transactionID = StringUtility.generateUUID();

        event1 = TransactionEventDTO.builder().date(eventDate2).eventType(EventTypeEnum.VALIDATION)
                .identificativoDoc(StringUtility.generateUUID())
                .identificativoPaziente("paziente-identifier-01")
                .identificativoSottomissione("submission-identifier-01")
                .validationResult(ValidationResultEnum.SYNTAX_ERROR).build();

        event2 = TransactionEventDTO.builder().date(eventDate3).eventType(EventTypeEnum.PUBLICATION)
        .identificativoDoc(StringUtility.generateUUID())
        .identificativoPaziente("paziente-identifier-01")
        .identificativoSottomissione("submission-identifier-01")
        .forcePublish(false).publicationResult(PublicationResultEnum.CDA_MATCH_ERROR).build();

        events = new ArrayList<>();
        events.add(event1);
        events.add(event2);

        transactionEvent = new TransactionEventsETY();
        transactionEvent = new TransactionEventsETY();
        transactionEvent.setTransactionID(transactionID);
        transactionEvent.setLastUpdate(eventDate3);
        transactionEvent.setLastEventType(EventTypeEnum.PUBLICATION);
        transactionEvent.setLastValidationResult(ValidationResultEnum.SYNTAX_ERROR);
        transactionEvent.setLastPublicationResult(PublicationResultEnum.CDA_MATCH_ERROR);
        transactionEvent.setEvents(events);

        validationEventsRepo.insert(transactionEvent);




    }


}


