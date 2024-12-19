 it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 *	Kafka consumer properties configuration.
 */
@Data
@Component
public class KafkaConsumerPropertiesCFG {
  
	/**
	 * Client id.
	 */
	@Value("${kafka.client-id}")
	private String clientId;

	/**
	 * Group id consumer.
	 */
	@Value("${kafka.consumer.group-id}")
	private String consumerGroupId;

	/**
	 * Consumer key deserializer.
	 */
	@Value("${kafka.consumer.key-deserializer}")
	private String consumerKeyDeserializer;

	/**
	 * Consumer value deserializer.
	 */
	@Value("${kafka.consumer.value-deserializer}")
	private String consumerValueDeserializer;

	/**
	 * Isolation level.
	 */
	@Value("${kafka.consumer.isolation.level}")
	private String isolationLevel;
	
	/**
	 * Flag autocommit.
	 */
	@Value("${kafka.consumer.auto-commit}")
	private String autoCommit;
	
	/**
	 * Flag auto offset reset.
	 */
	@Value("${kafka.consumer.auto-offset-reset}")
	private String autoOffsetReset;
	
	/**
	 * Eccezioni per dead letter.
	 */
	@Value("#{${kafka.consumer.dead-letter-exc}}")
	private List<String> deadLetterExceptions;
	
	/**
	 * Protocollo.
	 */
	@Value("${kafka.properties.security.protocol}")
	private String protocol;
	
	 
}
