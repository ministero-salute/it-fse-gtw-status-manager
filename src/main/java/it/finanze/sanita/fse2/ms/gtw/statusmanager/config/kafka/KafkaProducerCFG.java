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

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.StringUtility;

@Configuration
public class KafkaProducerCFG {

	/**
	 *	Kafka producer properties.
	 */
	@Autowired
	private KafkaProducerPropertiesCFG kafkaProducerPropCFG;


    /**
	 * Genera configurazione senza transazione.
	 * 
	 * @return	mappa configurazione producer senza transazione
	 */
	@Bean
	public Map<String, Object> producerWithoutTransactionConfigs() {
		Map<String, Object> props = new HashMap<>();
		
		props.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaProducerPropCFG.getClientId() + "-noTx");
		props.put(ProducerConfig.RETRIES_CONFIG, kafkaProducerPropCFG.getProducerRetries());
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProducerPropCFG.getProducerBootstrapServers());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProducerPropCFG.getProducerKeySerializer());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProducerPropCFG.getProducerValueSerializer());
		
		if(!StringUtility.isNullOrEmpty(kafkaProducerPropCFG.getProtocol())) {
			props.put("security.protocol", kafkaProducerPropCFG.getProtocol());
		}
		
		if(!StringUtility.isNullOrEmpty(kafkaProducerPropCFG.getMechanism())) {
			props.put("sasl.mechanism", kafkaProducerPropCFG.getMechanism());
		}
		
		if(!StringUtility.isNullOrEmpty(kafkaProducerPropCFG.getConfigJaas())) {
			props.put("sasl.jaas.config", kafkaProducerPropCFG.getConfigJaas());
		}
		
		if(!StringUtility.isNullOrEmpty(kafkaProducerPropCFG.getTrustoreLocation())) {
			props.put("ssl.truststore.location", kafkaProducerPropCFG.getTrustoreLocation());
		}
		
		if(!StringUtility.isNullOrEmpty(String.valueOf(kafkaProducerPropCFG.getTrustorePassword()))) {
			props.put("ssl.truststore.password", String.valueOf(kafkaProducerPropCFG.getTrustorePassword()));
		}
		
		return props;
	}

    /**
	 * Facotry dead producer.
	 * 
	 * @return	factory dead producer.
	 */
	@Bean
	public ProducerFactory<Object, Object> producerDeadFactory() {
		return new DefaultKafkaProducerFactory<>(producerWithoutTransactionConfigs());
	}

	/**
	 * Kafka template dead letter.
	 *
	 * @return	Kafka template
	 */
	@Bean
	@Qualifier("notxkafkadeadtemplate")
	public KafkaTemplate<Object, Object> noTxKafkaDeadTemplate() {
		return new KafkaTemplate<>(producerDeadFactory());
	}


	/**
	 * Factory dead letter.
	 * 
	 * @return	producer factory
	 */
	@Bean
	public ProducerFactory<String, String> producerFactoryWithoutTransaction() {
		return new DefaultKafkaProducerFactory<>(producerWithoutTransactionConfigs());
	}


	/**
	 * Ritorna il kafka template.
	 * 
	 * @return	template kafka
	 */
	@Bean
	@Qualifier("notxkafkatemplate")
	public KafkaTemplate<String, String> notxKafkaTemplate() {
		return new KafkaTemplate<>(producerFactoryWithoutTransaction());
	}

    
}
