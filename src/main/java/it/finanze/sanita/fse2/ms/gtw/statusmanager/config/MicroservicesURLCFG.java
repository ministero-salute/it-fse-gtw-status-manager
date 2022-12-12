/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class MicroservicesURLCFG {

	/**
	 * Data processor host.
	 */
	@Value("${ms.url.eds-processor-service}")
	private String processorHost;
	
	/**
	 * Config gtw host.
	 */
	@Value("${ms.url.gtw-config}")
	private String configHost;

}
