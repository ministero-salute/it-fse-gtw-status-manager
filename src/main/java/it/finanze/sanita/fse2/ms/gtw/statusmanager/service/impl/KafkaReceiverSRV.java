package it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.exceptions.BusinessException;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.IKafkaReceiverSRV;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.ITransactionEventsSRV;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KafkaReceiverSRV implements IKafkaReceiverSRV {

	@Autowired
	private ITransactionEventsSRV eventsSRV;
    
	@Override
	@KafkaListener(topics = "#{'${kafka.statusmanager.topic}'}",  clientIdPrefix = "#{'${kafka.client-id}'}", containerFactory = "kafkaListenerDeadLetterContainerFactory", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id}'}")
	public void listener(final ConsumerRecord<String, String> cr, final MessageHeaders messageHeaders) {
		log.debug("Consuming transaction event - Message received with key {}", cr.key());
		try {
			String workflowInstanceId = cr.key();
			String message = cr.value();
			srvListener(workflowInstanceId, message);
		} catch (Exception e) {
			deadLetterHelper(e);
			throw new BusinessException(e);
		}
	}
    
	public void srvListener(final String workflowInstanceId, final String message) {
		eventsSRV.saveEvent(workflowInstanceId, message);
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