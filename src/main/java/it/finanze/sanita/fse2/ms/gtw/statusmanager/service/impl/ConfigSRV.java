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
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.IConfigSRV;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.base.ClientRoutes.Config.PROPS_NAME_EXP_DAYS;
import static it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.base.ClientRoutes.Config.PROPS_NAME_ISSUER_CF;

@Service
public class ConfigSRV implements IConfigSRV {

	private static final Long DELTA_MS = 300000L;

	private final Map<String, Pair<Long, Object>> props;

	@Autowired
	private IConfigClient client;

	public ConfigSRV() {
		this.props = new HashMap<>();
	}
	
	@EventListener(ApplicationStartedEvent.class)
	void initialize() {
		refreshExpirationDate();
		refreshIsCfOnIssuerAllowed();
	}

	private void refreshExpirationDate() {
		int days = client.getExpirationDate();
		props.put(PROPS_NAME_EXP_DAYS, Pair.of(new Date().getTime(), days));
	}

	private void refreshIsCfOnIssuerAllowed() {
		boolean out = client.isCfOnIssuerAllowed();
		props.put(PROPS_NAME_ISSUER_CF, Pair.of(new Date().getTime(), out));
	}

	@Override
	public Integer getExpirationDate() {
		Pair<Long, Object> pair = props.getOrDefault(
			PROPS_NAME_EXP_DAYS,
			Pair.of(0L, null)
		);
		if (new Date().getTime() - pair.getKey() >= DELTA_MS) {
			synchronized(PROPS_NAME_EXP_DAYS) {
				refreshExpirationDate();
			}
		}
		return (Integer) props.get(PROPS_NAME_EXP_DAYS).getValue();
	}

	@Override
	public Boolean isCfOnIssuerAllowed() {
		Pair<Long, Object> pair = props.getOrDefault(
			PROPS_NAME_ISSUER_CF,
			Pair.of(0L, null)
		);
		if (new Date().getTime() - pair.getKey() >= DELTA_MS) {
			synchronized(PROPS_NAME_ISSUER_CF) {
				refreshIsCfOnIssuerAllowed();
			}
		}
		return (Boolean) props.get(PROPS_NAME_ISSUER_CF).getValue();
	}
}
