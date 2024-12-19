/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
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
	public void listenerGtw(final ConsumerRecord<String, String> cr, final MessageHeaders messageHeaders) {
		log.info("GTW LISTENER - Consuming transaction event - Message received with key {}", cr.key());
		abstractListener(cr);
	}
	
//	@Override
//	@KafkaListener(topics = "#{'${kafka.statusmanager.eds.topic}'}",  clientIdPrefix = "#{'${kafka.client-eds-id}'}", containerFactory = "kafkaListenerDeadLetterContainerFactoryEds", autoStartup = "${event.topic.auto.start}", groupId = "#{'${kafka.consumer.group-id}'}")
//	public void listenerEds(final ConsumerRecord<String, String> cr, final MessageHeaders messageHeaders) {
//		log.info("EDS LISTENER - Consuming transaction event - Message received with key {}", cr.key());
//		abstractListener(cr);
//	}

	private void abstractListener(ConsumerRecord<String, String> cr) {
		try {
			String workflowInstanceId = cr.key();
			String message = cr.value();
			srvListener(workflowInstanceId, message);
			log.info("END - Listener eds");
		} catch (Exception e) {
			log.error("Generic error while consuming eds msg");
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