package it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 *
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
	 * Producer bootstrap server.
	 */
	@Value("${kafka.producer.bootstrap-servers}")
	private String producerBootstrapServers;

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

	/**
	 * Protocol.
	 */
	@Value("${kafka.properties.security.protocol}")
	private String protocol;
	
	/**
	 * Meccanismo.
	 */
	@Value("${kafka.properties.sasl.mechanism}")
	private String mechanism;
	
	/**
	 * Config jass.
	 */
	@Value("${kafka.properties.sasl.jaas.config}")
	private String configJaas;
	
	/**
	 * Posizione trust store.
	 */
	@Value("${kafka.properties.ssl.truststore.location}")
	private String trustoreLocation;
	
	/**
	 * Password trust store.
	 */
	@Value("${kafka.properties.ssl.truststore.password}")
	private char[] trustorePassword;
	
	/**
	 * Flag ssl.
	 */
	@Value("${kafka.enablessl}")
	private boolean enableSsl;

}
