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
