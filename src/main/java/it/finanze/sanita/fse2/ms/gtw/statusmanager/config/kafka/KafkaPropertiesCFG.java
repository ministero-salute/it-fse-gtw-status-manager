package it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Configuration
@Data
public class KafkaPropertiesCFG {

	/**
	 * Producer bootstrap server.
	 */
	@Value("${kafka.bootstrap-servers}")
	private String bootstrapServers;
	
	@Value("${kafka.oauth.tenantId}")
	private String tenantId;

	@Value("${kafka.oauth.appId}")
	private String appId;

	@Value("${kafka.oauth.pwd}")
	private String pwd;

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
	 
}
