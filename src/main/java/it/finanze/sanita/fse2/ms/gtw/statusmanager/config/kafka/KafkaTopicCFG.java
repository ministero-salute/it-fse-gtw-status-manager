package it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 *	@author vincenzoingenito
 *
 *	Kafka topic configuration.
 */
@Data
@Component
public class KafkaTopicCFG {

	/**
	 * Topic.
	 */
	@Value("${kafka.statusmanager.topic}")
	private String statusManagerTopic;
}
