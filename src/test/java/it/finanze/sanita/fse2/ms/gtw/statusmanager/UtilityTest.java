package it.finanze.sanita.fse2.ms.gtw.statusmanager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.Constants;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka.KafkaPropertiesCFG;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response.LogTraceInfoDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.response.ResponseDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ActivityEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.EventTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.PublicationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.RawValidationEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.RegionCodeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ValidationResultEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.ValidationException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.repository.impl.TransactionEventsRepo;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl.KafkaReceiverSRV;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.StringUtility;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Constants.Profile.TEST)
class UtilityTest {
	
	@Autowired
	private transient MongoTemplate mongoTemplate;

	@Autowired
	private KafkaReceiverSRV kafkaSRV;
	
	@Autowired
	KafkaPropertiesCFG kafkaCFG;
		
	@Test
	@DisplayName("String utility null -> exception")
	void testStringUtilityException() throws Exception{
		
		boolean flag = StringUtility.isNullOrEmpty("");
		assertTrue(flag);
		
		boolean flag1 = StringUtility.isNullOrEmpty(null);
		assertTrue(flag1);
		
		boolean flag2 = StringUtility.isNullOrEmpty("aaaaa");
		assertFalse(flag2);
	}
	
	@Test
	@DisplayName("String utility null -> exception")
	void encodeSHA256B64Exception() throws Exception{
		
		//exception encode
		String convertion = null;
		try {
			convertion = StringUtility.encodeSHA256B64(null);
		} catch (Exception e) {
			Assertions.assertNull(convertion);
		}
		assertThrows(BusinessException.class, () -> StringUtility.encodeSHA256B64(null));

		//encode OK
		String convertion1 = null;
		convertion1 = StringUtility.encodeSHA256B64("aaa");
		assertNotNull(convertion1);
		
	}
	
	@Test
	@DisplayName("String utility -> OK")
	void encodeSHA256B64Ok() {

		//encode OK
		String convertion1 = null;
		convertion1 = StringUtility.encodeSHA256B64("aaa");
		assertNotNull(convertion1);

	}
	
	

	@Test
	@DisplayName("String utility HEX null -> exception")
	void encodeSHA256HEXException() throws Exception{
		
		//exception encode
		String convertion = null;
		try {
			convertion = StringUtility.encodeSHA256Hex(null);
		} catch (Exception e) {
			Assertions.assertNull(convertion);
		}
		assertThrows(BusinessException.class, () -> StringUtility.encodeSHA256Hex(null));

	}
	
	@Test
	@DisplayName("String utility 256 HEX-> OK")
	void encodeSHA256HEXOk() {

		//encode OK
		String strToConvert = "0";
		String convertion = StringUtility.encodeSHA256Hex(strToConvert);
		assertNotNull(convertion);

		//encode wrong case
		String strToConvert2 = "1aa";
		String convertion2 = StringUtility.encodeSHA256Hex(strToConvert2);
		assertNotNull(convertion2);
		
	}
	
	@Test
	@DisplayName("String utility date format not valid -> exception")
	void dataFormatConvertException() {
		String data = "abcd";
		
		// Data null
		assertThrows(ValidationException.class, () -> StringUtility.dateFormatValid(data));
		
		// Data sbagliata
		assertThrows(ValidationException.class, () -> StringUtility.dateFormatValid(null));	
	}
	
	@Test
	@DisplayName("String utility date format  valid Ok")
	void dataFormatOk() {
		String data = "2012-01-01";

		assertDoesNotThrow(()->StringUtility.dateFormatValid(data));
		
	}

	@Test
	@DisplayName("enumeration test ")
	void enumTest() {
		
		for(RegionCodeEnum entry : Arrays.asList(RegionCodeEnum.values())) {
			assertNotNull(entry.getCode());
			assertNotNull(entry.getDescription());
			}
		
		for(PublicationResultEnum entry1 : Arrays.asList(PublicationResultEnum.values())) {
			assertNotNull(entry1.getTitle());
			assertNotNull(entry1.getType());
			}
		
		for(ValidationResultEnum entry2 : Arrays.asList(ValidationResultEnum.values())) {
			assertNotNull(entry2.getTitle());
			assertNotNull(entry2.getType());
			}
		
		for(EventTypeEnum entry3 : Arrays.asList(EventTypeEnum.values())) {
			assertNotNull(entry3.getCode());

			}
		
		for(ActivityEnum entry4 : Arrays.asList(ActivityEnum.values())) {
			assertNotNull(entry4.getCode());
			}
		
		for(RawValidationEnum entry5 : Arrays.asList(RawValidationEnum.values())) {
			assertNotNull(entry5.getCode());
			assertNotNull(entry5.getDescription());
			}
	}

	
	@Test
	@DisplayName("transaction Events Repo test Ko ")
	void transactionEvTestKo() {
		TransactionEventsRepo transEvRep = new TransactionEventsRepo();
		assertThrows(BusinessException.class, () -> transEvRep.saveEvent(null, null));
	}
 
	
	@Test
	@DisplayName("Test transaction events")
	void testTransEvs() {
		
		//first constructor
		
		LogTraceInfoDTO logInfo = new LogTraceInfoDTO("abc", "def");
		ResponseDTO respDTO = new ResponseDTO(logInfo);
		
		assertNotNull(respDTO.getSpanID());
		assertNotNull(respDTO.getTraceID());

		//second constructor
		
		ResponseDTO respDTO2 = new ResponseDTO(logInfo, 1, null);

		assertNotNull(respDTO2.getError());
		assertNotNull(respDTO2.getSpanID());
		assertNotNull(respDTO2.getTraceID());

	}
	
}
