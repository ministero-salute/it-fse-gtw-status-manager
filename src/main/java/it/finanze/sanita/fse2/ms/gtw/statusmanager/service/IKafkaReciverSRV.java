package it.finanze.sanita.fse2.ms.gtw.statusmanager.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.messaging.MessageHeaders;

public interface IKafkaReciverSRV {

	/**
	 * Kafka listener
	 * @param cr
	 * @param messageHeaders
	 */
	void listener(ConsumerRecord<String, String> cr, MessageHeaders messageHeaders);

}