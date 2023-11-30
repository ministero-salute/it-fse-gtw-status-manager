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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static it.finanze.sanita.fse2.ms.gtw.statusmanager.client.routes.base.ClientRoutes.Config.*;

@Slf4j
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
		refreshIsSubjectPersistenceEnabled();
		runningConfiguration();
	}

	private void refreshExpirationDate() {
		int days = client.getExpirationDate();
		props.put(PROPS_NAME_EXP_DAYS, Pair.of(new Date().getTime(), days));
	}

	private void refreshIsSubjectPersistenceEnabled(){
		boolean out = client.isSubjectPersistenceEnabled();
		props.put(PROPS_NAME_SUBJECT, Pair.of(new Date().getTime(), out));
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
				verifyExpirationDate(pair);
			}
		}
		return (Integer) props.get(PROPS_NAME_EXP_DAYS).getValue();
	}

	@Override
	public Boolean isSubjectPersistenceEnabled() {
		Pair<Long, Object> pair = props.getOrDefault(
				PROPS_NAME_SUBJECT,
				Pair.of(0L, null)
		);
		if (new Date().getTime() - pair.getKey() >= DELTA_MS) {
			synchronized (PROPS_NAME_SUBJECT) {
				refreshIsSubjectPersistenceEnabled();
				verifyIsSubjectPersistenceEnabled(pair);
			}
		}
		return (Boolean) props.get(PROPS_NAME_SUBJECT).getValue();
	}

	@Override
	public Boolean isCfOnIssuerNotAllowed() {
		Pair<Long, Object> pair = props.getOrDefault(
			PROPS_NAME_ISSUER_CF,
			Pair.of(0L, null)
		);
		if (new Date().getTime() - pair.getKey() >= DELTA_MS) {
			synchronized(PROPS_NAME_ISSUER_CF) {
				refreshIsCfOnIssuerAllowed();
				verifyIsCfOnIssuerAllowed(pair);
			}
		}
		return (Boolean) props.get(PROPS_NAME_ISSUER_CF).getValue();
	}

	private void runningConfiguration() {
		props.forEach((id, pair) -> log.info("[GTW-CONFIG] key: {} | value: {}", id, pair.getValue()));
	}

	private void verifyExpirationDate(Pair<Long, Object> pair) {
		int previous = (int) pair.getValue();
		int current = (int) props.get(PROPS_NAME_EXP_DAYS).getValue();
		if(previous != current) {
			log.info("[GTW-CONFIG][UPDATE] key: {} | value: {} (previous: {})", PROPS_NAME_EXP_DAYS, current, previous);
		}
	}

	private void verifyIsSubjectPersistenceEnabled(Pair<Long, Object> pair){
		Boolean previous = (Boolean) pair.getValue();
		boolean current = (boolean) props.get(PROPS_NAME_SUBJECT).getValue();
		if (previous != current){
			log.info("[GTW-CONFIG][UPDATE] key: {} | value: {} (previous: {})", PROPS_NAME_SUBJECT, current, previous);
		}
	}

	private void verifyIsCfOnIssuerAllowed(Pair<Long, Object> pair) {
		Boolean previous = (Boolean) pair.getValue();
		boolean current = (boolean) props.get(PROPS_NAME_ISSUER_CF).getValue();
		if(previous != current) {
			log.info("[GTW-CONFIG][UPDATE] key: {} | value: {} (previous: {})", PROPS_NAME_ISSUER_CF, current, previous);
		}
	}
}
