 it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 *	Kafka producer properties configuration.
 */
@Data
@Component
public class KafkaProducerPropertiesCFG {

	/**
	 * Proprietà generica.
	 */
	@Value("${kafka.client-id}")
	private String clientId;

	/**
	 * Proprietà producer.
	 */
	@Value("${kafka.producer.key-serializer}")
	private String producerKeySerializer;

	/**
	 * Tentativi producer.
	 */
	@Value("${kafka.producer.retries}")
	private String producerRetries;

	/**
	 * Producer serializer.
	 */
	@Value("${kafka.producer.value-serializer}")
	private String producerValueSerializer;


	/**
	 * Producer trans id.
	 */
	@Value("${kafka.producer.transactional.id}")
	private String producerTransactionalId;
	
	/**
	 * Flag idempotence.
	 */
	@Value("${kafka.producer.enable.idempotence}")
	private Boolean producerIdempotence;
	
	/**
	 * se settatp ad all un evento è inviato solo quanto tutti i membri del cluster kafka sono allineati.
	 */
	@Value("${kafka.producer.ack}")
	private String producerACK;

	
}
