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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.config.kafka.oauth2.CustomAuthenticateCallbackHandler;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.utility.StringUtility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class KafkaConsumerCFG {

	/**
	 *	Kafka consumer properties.
	 */
	@Autowired
	private KafkaConsumerPropertiesCFG kafkaConsumerPropCFG;
	
	@Autowired
	private KafkaTopicCFG topicCFG;
	
	@Autowired
	private KafkaPropertiesCFG kafkaPropsCfg;


	/**
	 * Configurazione consumer.
	 * 
	 * @return	configurazione consumer
	 */
	@Bean
	public Map<String, Object> consumerConfigs() {
		Map<String, Object> props = new HashMap<>();
		
		props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConsumerPropCFG.getClientId());
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPropsCfg.getBootstrapServers());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerPropCFG.getConsumerGroupId());
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaConsumerPropCFG.getConsumerKeyDeserializer());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConsumerPropCFG.getConsumerValueDeserializer());
		props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, kafkaConsumerPropCFG.getIsolationLevel());
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaConsumerPropCFG.getAutoCommit());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerPropCFG.getAutoOffsetReset());
		
		if(!StringUtility.isNullOrEmpty(kafkaConsumerPropCFG.getProtocol())) {
			props.put("security.protocol", kafkaConsumerPropCFG.getProtocol());
		}
		
		if(!StringUtility.isNullOrEmpty(kafkaPropsCfg.getMechanism())) {
			props.put("sasl.mechanism", kafkaPropsCfg.getMechanism());
		}
		
		if(!StringUtility.isNullOrEmpty(kafkaPropsCfg.getConfigJaas())) {
			props.put("sasl.jaas.config", kafkaPropsCfg.getConfigJaas());
		}
		
		if (!StringUtility.isNullOrEmpty(kafkaPropsCfg.getTrustoreLocation())) {
			props.put("ssl.truststore.location", kafkaPropsCfg.getTrustoreLocation());
		}
		if (kafkaPropsCfg.getTrustorePassword() != null && kafkaPropsCfg.getTrustorePassword().length > 0) {
			props.put("ssl.truststore.password", String.valueOf(kafkaPropsCfg.getTrustorePassword()));
		}
		
		if("OAUTHBEARER".equals(kafkaPropsCfg.getMechanism())) {
			props.put("sasl.login.callback.handler.class", CustomAuthenticateCallbackHandler.class);
			props.put("kafka.oauth.tenantId", kafkaPropsCfg.getTenantId());	
			props.put("kafka.oauth.appId", kafkaPropsCfg.getAppId());	
			props.put("kafka.oauth.pfxPathName", kafkaPropsCfg.getPfxPathName());	
			props.put("kafka.oauth.pwd", kafkaPropsCfg.getPwd());	
		}

		
		return props;
	}


	/**
	 * Consumer factory.
	 * 
	 * @return	factory
	 */
	@Bean
	public ConsumerFactory<String, String> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs());
	}

	/**
	 * Factory with dead letter configuration.
	 * 
	 * @param deadLetterKafkaTemplate
	 * @return	factory
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerDeadLetterContainerFactory(final @Qualifier("notxkafkadeadtemplate") KafkaTemplate<Object, Object> deadLetterKafkaTemplate) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		
		DeadLetterPublishingRecoverer dlpr = new DeadLetterPublishingRecoverer(deadLetterKafkaTemplate, (consumerRecord, ex) -> new TopicPartition(topicCFG.getStatusManagerTopicDlt(), -1));
		// Set classificazione errori da gestire per la deadLetter.
		DefaultErrorHandler sceh = new DefaultErrorHandler(dlpr, new FixedBackOff(FixedBackOff.DEFAULT_INTERVAL, FixedBackOff.UNLIMITED_ATTEMPTS));
		
		log.debug("Setting dead letter classification");
		setClassification(sceh);
		
		// da eliminare se non si volesse gestire la dead letter
		factory.setCommonErrorHandler(sceh); 

		return factory;
	}
	
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerDeadLetterContainerFactoryEds(final @Qualifier("notxkafkadeadtemplate") KafkaTemplate<Object, Object> deadLetterKafkaTemplate) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		
		DeadLetterPublishingRecoverer dlpr = new DeadLetterPublishingRecoverer(deadLetterKafkaTemplate, (consumerRecord, ex) -> new TopicPartition(topicCFG.getStatusManagerEdsTopicDlt(), -1));
		// Set classificazione errori da gestire per la deadLetter.
		DefaultErrorHandler sceh = new DefaultErrorHandler(dlpr, new FixedBackOff(FixedBackOff.DEFAULT_INTERVAL, FixedBackOff.UNLIMITED_ATTEMPTS));
		
		log.debug("Setting dead letter classification");
		setClassification(sceh);
		
		// da eliminare se non si volesse gestire la dead letter
		factory.setCommonErrorHandler(sceh); 

		return factory;
	}
	
	private void setClassification(final DefaultErrorHandler sceh) {
		List<Class<? extends Exception>> out = getExceptionsConfig();

		for (Class<? extends Exception> ex : out) {
			log.warn("Found a non retryable exception: {}", ex.getCanonicalName());
			sceh.addNotRetryableExceptions(ex);
		}
		
	}

	/**
	 * @return	exceptions list
	 */
	@SuppressWarnings("unchecked")
	private List<Class<? extends Exception>> getExceptionsConfig() {
		List<Class<? extends Exception>> out = new ArrayList<>();
		String temp = null;
		try {
			for (String excs : kafkaConsumerPropCFG.getDeadLetterExceptions()) {
				temp = excs;
				Class<? extends Exception> s = (Class<? extends Exception>) Class.forName(excs, false, Thread.currentThread().getContextClassLoader());
				out.add(s);
			}
		} catch (Exception e) {
			log.error("Error retrieving the exception with fully qualified name: <{}>", temp);
			log.error("Error : ", e);
		}
		
		return out;
	}
	
	/**
	 * Default Container factory.
	 * 
	 * @param kafkaTemplate	templete
	 * @return				factory
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory(final @Qualifier("notxkafkatemplate") KafkaTemplate<String, String> kafkaTemplate) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		
		return factory;
	}
}
