package it.finanze.sanita.fse2.ms.gtw.statusmanager;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka.KafkaPropertiesCFG;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.KafkaMessageDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.PublicationInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.ValidationCDAInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.entity.TransactionEventsETY;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.impl.TransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl.KafkaReceiverSRV;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.EncryptDecryptUtility;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.StringUtility;

/**
 * Test kafka receiver and Validation Event Service
 */
@SpringBootTest
@ActiveProfiles(Constants.Profile.TEST)
class TransactionEventTest {

	@Autowired
	private KafkaReceiverSRV kafkaSRV;

	@Autowired
	private KafkaPropertiesCFG kafkaCFG;

	@Autowired
	private TransactionEventsRepo transactionEventsRepo;

	@Autowired
	private MongoTemplate mongoTemplate;

	@BeforeEach
    void setup() {
		mongoTemplate.dropCollection(TransactionEventsETY.class);
	}


	/**
	 * Save transaction events received by kafka
	 * Two events with the same txID are receveid, so the same ety is updated
	 */
	@Test
	@DisplayName("Save transaction event")
	void t1() {
        Boolean result = true;
        try {

			final String transactionID = StringUtility.generateUUID();

			// ---- First Event

    		final ConsumerRecord<String, String> cr = getRecord(transactionID, false);
    		final MessageHeaders messageHeaders = getHeader();
    		kafkaSRV.listener(cr, messageHeaders);

			List<TransactionEventsETY> transactionEvents = transactionEventsRepo.findAll();
			assertTrue(1 == transactionEvents.size(), "Should be present only one entry at database");
			assertTrue(transactionEvents.get(0).getLastUpdate().equals(transactionEvents.get(0).getEvents().get(0).getDate()), "last_update Date must be equal to the event date just entered");
			assertTrue(transactionEvents.get(0).getLastValidationResult().equals(transactionEvents.get(0).getEvents().get(0).getValidationResult()), "last_validation_result must be equal to the activity just entered");

			// ---- Second Event

			final ConsumerRecord<String, String> cr1 = getRecord(transactionID, true);
    		final MessageHeaders messageHeaders1 = getHeader();
			kafkaSRV.listener(cr1, messageHeaders1);

			transactionEvents = transactionEventsRepo.findAll();
			assertTrue(1 == transactionEvents.size(), "Should be present only one entry at database");
			assertTrue(2 == transactionEvents.get(0).getEvents().size(), "The validation events array size should be 2");
			assertTrue(transactionEvents.get(0).getLastUpdate().equals(transactionEvents.get(0).getEvents().get(1).getDate()), "last_update Date must be equal to the second event date entered");
			assertTrue(transactionEvents.get(0).getLastPublicationResult().equals(transactionEvents.get(0).getEvents().get(1).getPublicationResult()), "last_publication_result must be equal to the second transaction event result just entered");
			

        } catch (Exception e) {
            result = false;
        }

        Assertions.assertTrue(result);
	}

	/**
	 * Save transaction events for historical document received by kafka
	 */
	@Test
	@DisplayName("Save transaction event (historical document)")
	void t2() {
        Boolean result = true;
        try {

			final String transactionID = StringUtility.generateUUID();

			// ---- Validation Event

    		final ConsumerRecord<String, String> cr = getHistoricalDocRecord(transactionID, false);
    		final MessageHeaders messageHeaders = getHeader();
    		kafkaSRV.listener(cr, messageHeaders);

			List<TransactionEventsETY> transactionEvents = transactionEventsRepo.findAll();
			assertTrue(1 == transactionEvents.size(), "Should be present only one entry at database");
			assertTrue(transactionEvents.get(0).getLastUpdate().equals(transactionEvents.get(0).getEvents().get(0).getDate()), "last_update Date must be equal to the event date just entered");
			assertTrue(transactionEvents.get(0).getLastValidationResult().equals(transactionEvents.get(0).getEvents().get(0).getValidationResult()), "last_validation_result must be equal to the activity just entered");
			assertTrue(transactionEvents.get(0).getLastEventType().equals(transactionEvents.get(0).getEvents().get(0).getEventType()));

			// ---- Publish Event

			final ConsumerRecord<String, String> cr1 = getHistoricalDocRecord(transactionID, true);
    		final MessageHeaders messageHeaders1 = getHeader();
			kafkaSRV.listener(cr1, messageHeaders1);

			transactionEvents = transactionEventsRepo.findAll();
			assertTrue(1 == transactionEvents.size(), "Should be present only one entry at database");
			assertTrue(2 == transactionEvents.get(0).getEvents().size(), "The validation events array size should be 2");
			assertTrue(transactionEvents.get(0).getLastUpdate().equals(transactionEvents.get(0).getEvents().get(1).getDate()), "last_update Date must be equal to the second event date entered");
			assertTrue(transactionEvents.get(0).getLastPublicationResult().equals(transactionEvents.get(0).getEvents().get(1).getPublicationResult()), "last_publication_result must be equal to the second transaction event result just entered");
			assertTrue(transactionEvents.get(0).getLastEventType().equals(transactionEvents.get(0).getEvents().get(1).getEventType()));
			

        } catch (Exception e) {
            result = false;
        }

        Assertions.assertTrue(result);
	}


	private MessageHeaders getHeader() {
		final MessageHeaders messageHeaders = new MessageHeaders(new HashMap<String, Object>());
		return messageHeaders;
	}

	private ConsumerRecord<String, String> getRecord(String transactionID, Boolean isUpdateRecord) {

		KafkaMessageDTO km = null;
		String key = "";

		if (!isUpdateRecord) {

			ValidationCDAInfoDTO validationEvent = ValidationCDAInfoDTO.builder()
			.transactionID(transactionID).activity(ActivityEnum.VALIDATION)
			.identificativoDoc(StringUtility.generateUUID())
			.identificativoPaziente(StringUtility.generateUUID())
			.identificativoSottomissione(StringUtility.generateUUID()).build();


			km = KafkaMessageDTO.builder().validationInfo(validationEvent)
					.validationResult(ValidationResultEnum.SEMANTIC_ERROR).build();
			key = "V";
		} else {

			PublicationInfoDTO publicationInfo = PublicationInfoDTO.builder().transactionID(transactionID)
			.identificativoDoc(StringUtility.generateUUID())
			.identificativoPaziente(StringUtility.generateUUID())
			.identificativoSottomissione(StringUtility.generateUUID()).forcePublish(false).build();


			km = KafkaMessageDTO.builder().publicationInfo(publicationInfo).publicationResult(PublicationResultEnum.PUBLISHING_ERROR)
					.build();

			key = "P";
		}

		String value = EncryptDecryptUtility.encryptObject(kafkaCFG.getCrypto(), km);
		final ConsumerRecord<String, String> cr = new ConsumerRecord<String, String>("", 0, 0, key, value);
		return cr;
	}

	private ConsumerRecord<String, String> getHistoricalDocRecord(String transactionID, Boolean isUpdateRecord) {

		KafkaMessageDTO km = null;
		String key = "";

		if (!isUpdateRecord) {

			ValidationCDAInfoDTO validationEvent = ValidationCDAInfoDTO.builder()
			.transactionID(transactionID).activity(ActivityEnum.PRE_PUBLISHING)
			.identificativoDoc(StringUtility.generateUUID())
			.identificativoPaziente(StringUtility.generateUUID())
			.identificativoSottomissione(StringUtility.generateUUID()).build();


			km = KafkaMessageDTO.builder().validationInfo(validationEvent).validationResult(ValidationResultEnum.OK).build();
			key = "HV";
		} else {

			PublicationInfoDTO publicationInfo = PublicationInfoDTO.builder().transactionID(transactionID)
			.identificativoDoc(StringUtility.generateUUID())
			.identificativoPaziente(StringUtility.generateUUID())
			.identificativoSottomissione(StringUtility.generateUUID()).forcePublish(false).build();


			km = KafkaMessageDTO.builder().publicationInfo(publicationInfo).publicationResult(PublicationResultEnum.OK).build();

			key = "HP";
		}

		String value = EncryptDecryptUtility.encryptObject(kafkaCFG.getCrypto(), km);
		final ConsumerRecord<String, String> cr = new ConsumerRecord<String, String>("", 0, 0, key, value);
		return cr;
	}
	

}
