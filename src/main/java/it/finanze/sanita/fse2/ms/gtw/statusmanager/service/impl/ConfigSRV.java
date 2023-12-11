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
package it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.ConfigItemDTO;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ConfigItemTypeEnum;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.IConfigSRV;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.base.ClientRoutes.Config.*;
import static it.finanze.sanita.fse2.ms.gtw.statusmanager.dto.ConfigItemDTO.ConfigDataItemDTO;
import static it.finanze.sanita.fse2.ms.gtw.statusmanager.enums.ConfigItemTypeEnum.STATUS_MANAGER;

@Slf4j
@Service
public class ConfigSRV implements IConfigSRV {

	private static final Long DELTA_MS = 300000L;

	@Autowired
	private IConfigClient client;

	private final Map<String, Pair<Long, String>> props;

	public ConfigSRV() {
		this.props = new HashMap<>();
	}
	
	@EventListener(ApplicationStartedEvent.class)
	void initialize() {
		for(ConfigItemTypeEnum en : ConfigItemTypeEnum.values()) {
			log.info("[GTW-CFG] Retrieving {} properties ...", en.name());
			ConfigItemDTO items = client.getConfigurationItems(en);
			List<ConfigDataItemDTO> opts = items.getConfigurationItems();
			for(ConfigDataItemDTO opt : opts) {
				opt.getItems().forEach((key, value) -> {
					log.info("[GTW-CFG] Property {} is set as {}", key, value);
					props.put(key, Pair.of(new Date().getTime(), value));
				});
			}
		}
		integrity();
	}

	@Override
	public Integer getExpirationDate() {
		long lastUpdate = props.get(PROPS_NAME_EXP_DAYS).getKey();
		if (new Date().getTime() - lastUpdate >= DELTA_MS) {
			synchronized(PROPS_NAME_EXP_DAYS) {
				if (new Date().getTime() - lastUpdate >= DELTA_MS) {
					refresh(STATUS_MANAGER, PROPS_NAME_EXP_DAYS);
				}
			}
		}
		return Integer.parseInt(
			props.get(PROPS_NAME_EXP_DAYS).getValue()
		);
	}

	@Override
	public Boolean isSubjectPersistenceEnabled() {
		long lastUpdate = props.get(PROPS_NAME_SUBJECT).getKey();
		if (new Date().getTime() - lastUpdate >= DELTA_MS) {
			synchronized (PROPS_NAME_SUBJECT) {
				if (new Date().getTime() - lastUpdate >= DELTA_MS) {
					refresh(STATUS_MANAGER, PROPS_NAME_SUBJECT);
				}
			}
		}
		return Boolean.parseBoolean(
			props.get(PROPS_NAME_SUBJECT).getValue()
		);
	}

	@Override
	public Boolean isCfOnIssuerNotAllowed() {
		long lastUpdate = props.get(PROPS_NAME_ISSUER_CF).getKey();
		if (new Date().getTime() - lastUpdate >= DELTA_MS) {
			synchronized(PROPS_NAME_ISSUER_CF) {
				if (new Date().getTime() - lastUpdate >= DELTA_MS) {
					refresh(STATUS_MANAGER, PROPS_NAME_ISSUER_CF);
				}
			}
		}
		return Boolean.parseBoolean(
			props.get(PROPS_NAME_ISSUER_CF).getValue()
		);
	}

	private void refresh(ConfigItemTypeEnum type, String name) {
		String previous = props.getOrDefault(name, Pair.of(0L, null)).getValue();
		String prop = client.getProps(type, name, previous);
		props.put(name, Pair.of(new Date().getTime(), prop));
	}

	private void integrity() {
		String err = "Missing props {} from status-manager";
		String[] out = new String[]{
			PROPS_NAME_EXP_DAYS,
			PROPS_NAME_SUBJECT,
			PROPS_NAME_ISSUER_CF
		};
		for (String prop : out) {
			if(!props.containsKey(prop)) throw new IllegalStateException(err.replace("{}", prop));
		}
	}
}
