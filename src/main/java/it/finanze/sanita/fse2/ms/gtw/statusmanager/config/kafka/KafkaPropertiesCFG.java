
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
	
	@Value("${kafka.oauth.pfxPathName}")
	private String pfxPathName;

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
