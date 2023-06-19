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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka;

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
	 * Consumer bootstrap server.
	 */
	@Value("${kafka.consumer.bootstrap-servers}")
	private String consumerBootstrapServers;
	
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
	
	/**
	 * Meccanismo.
	 */
	@Value("${kafka.properties.sasl.mechanism}")
	private String mechanism;
	
	/**
	 * Config jaas.
	 */
	@Value("${kafka.properties.sasl.jaas.config}")
	private String configJaas;
	
	/**
	 * Truststore location.
	 */
	@Value("${kafka.properties.ssl.truststore.location}")
	private String trustoreLocation;
	
	/**
	 * Truststore password.
	 */
	@Value("${kafka.properties.ssl.truststore.password}")
	private transient char[] trustorePassword;
	
}
