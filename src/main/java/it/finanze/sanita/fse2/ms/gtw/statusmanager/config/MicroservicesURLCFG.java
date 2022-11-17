/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Getter
public class MicroservicesURLCFG {

	/**
	 * Data processor host.
	 */
	@Value("${ms.url.eds-processor-service}")
	private String processorHost;

	/**
	 * Definizione rest template.
	 *
	 * @return	rest template
	 */
	@Bean
	@Qualifier("restTemplate")
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
