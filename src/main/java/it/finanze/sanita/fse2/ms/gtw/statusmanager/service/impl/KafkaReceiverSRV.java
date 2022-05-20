package it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka.KafkaPropertiesCFG;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.KafkaMessageDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.IKafkaReciverSRV;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.EncryptDecryptUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaReceiverSRV implements IKafkaReciverSRV {
	
	@Autowired
	private KafkaPropertiesCFG kafkaPropCFG;

	@Autowired
	private TransactionEventsSRV validationEventsSRV;
    


	@Override
	@KafkaListener(topics = "#{'${kafka.topic}'}",  clientIdPrefix = "#{'${kafka.client-id}'}", containerFactory = "kafkaListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id}'}")
	public void listener(final ConsumerRecord<String, String> cr, final MessageHeaders messageHeaders) {
    	String message = cr.value();
        log.info("Consuming Transaction Event - Message received with key {}", cr.key());
        
        try {
        	KafkaMessageDTO km = EncryptDecryptUtility.decryptObject(kafkaPropCFG.getCrypto(), message, KafkaMessageDTO.class);

			if(cr.key().equals("V")) {
				if (km != null && km.getValidationInfo() != null && km.getValidationInfo().getTransactionID() != null && km.getValidationResult() != null) {

					log.info("Received Validation Event with transaction ID: " + km.getValidationInfo().getTransactionID());
	
					validationEventsSRV.saveValidationEvent(km.getValidationInfo(), km.getValidationResult(), false, false);
					
				} else {
					log.warn("Error consuming Validation Event with key {}: null received", cr.key());
				}

			} else if(cr.key().equals("HV")) {
				if (km != null && km.getValidationInfo() != null && km.getValidationInfo().getTransactionID() != null && km.getValidationResult() != null) {

					log.info("Received Historical Document Validation Event with transaction ID: " + km.getValidationInfo().getTransactionID());
	
					validationEventsSRV.saveValidationEvent(km.getValidationInfo(), km.getValidationResult(), true, false);
					
				} else {
					log.warn("Error consuming Historical Document Validation Event with key {}: null received", cr.key());
				}

			} else if(cr.key().equals("TSV")) {
				if (km != null && km.getValidationInfo() != null && km.getValidationInfo().getTransactionID() != null && km.getValidationResult() != null) {

					log.info("Received TS Document Validation Event with transaction ID: " + km.getValidationInfo().getTransactionID());
	
					validationEventsSRV.saveValidationEvent(km.getValidationInfo(), km.getValidationResult(), false, true);
					
				} else {
					log.warn("Error consuming TS Document Validation Event with key {}: null received", cr.key());
				}

			} else if(cr.key().equals("P")){

				if (km != null && km.getPublicationInfo() != null && km.getPublicationInfo().getTransactionID() != null && km.getPublicationResult() != null) {

					log.info("Received Publication Event with transaction ID: " + km.getPublicationInfo().getTransactionID());

					validationEventsSRV.savePublicationEvent(km.getPublicationInfo(), km.getPublicationResult(), false, false);
					
				} else {
					log.warn("Error consuming Publication Event with key {}: null received", cr.key());
				}

			} else if(cr.key().equals("HP")){

				if (km != null && km.getPublicationInfo() != null && km.getPublicationInfo().getTransactionID() != null && km.getPublicationResult() != null) {

					log.info("Received Historical Document Publication Event with transaction ID: " + km.getPublicationInfo().getTransactionID());

					validationEventsSRV.savePublicationEvent(km.getPublicationInfo(), km.getPublicationResult(), true, false);
					
				} else {
					log.warn("Error consuming Historical Document Publication Event with key {}: null received", cr.key());
				}

			} else if(cr.key().equals("TSP")){

				if (km != null && km.getPublicationInfo() != null && km.getPublicationInfo().getTransactionID() != null && km.getPublicationResult() != null) {

					log.info("Received TS Document Publication Event with transaction ID: " + km.getPublicationInfo().getTransactionID());

					validationEventsSRV.savePublicationEvent(km.getPublicationInfo(), km.getPublicationResult(), false, true);
					
				} else {
					log.warn("Error consuming TS Document Publication Event with key {}: null received", cr.key());
				}

			} else {
				log.warn("Error consuming Transaction, unknown key {}", cr.key());
			}

		} catch (Exception e) {
			deadLetterHelper(e);
			throw new BusinessException(e);
		}
    }
    

	/**
	 * @param e
	 */
	private void deadLetterHelper(Exception e) {
		StringBuilder sb = new StringBuilder("LIST OF USEFUL EXCEPTIONS TO MOVE TO DEADLETTER OFFSET 'kafka.consumer.dead-letter-exc'. ");
		boolean continua = true;
		Throwable excTmp = e;
		Throwable excNext = null;

		while (continua) {
		
			if (excNext != null) {
				excTmp = excNext;
				sb.append(", ");
			}
			
			sb.append(excTmp.getClass().getCanonicalName());
			excNext = excTmp.getCause();
			
			if (excNext == null) {
				continua = false;
			}
			
		}
		
		log.error("{}", sb.toString());
	}
	
 
   
}