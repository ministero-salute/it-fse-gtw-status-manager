/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.messaging.MessageHeaders;

public interface IKafkaReceiverSRV {

	/**
	 * Kafka listener
	 * @param cr
	 * @param messageHeaders
	 */
	void listener(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders);

}