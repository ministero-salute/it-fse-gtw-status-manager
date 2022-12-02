/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.client.IConfigClient;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.service.IConfigSRV;

@Service
public class ConfigSRV implements IConfigSRV {

	private static final Long DELTA_MS = 300000L;
	
	@Autowired
	private IConfigClient configClient;

	private Integer expirationDate;
	
	private Long lastUpdate;
	
	private final Object lockObj = new Object();
	
	@Async
	@EventListener(ApplicationStartedEvent.class)
	void initialize() {
		refreshExpirationDate();
		lastUpdate = new Date().getTime();
	}

	private void refreshExpirationDate() {
		expirationDate = configClient.getExpirationDate();
	}

	@Override
	public Integer getExpirationDate() {
		Long passedTime = new Date().getTime() - lastUpdate;
		if (passedTime>=DELTA_MS) {
			synchronized(lockObj) {
				refreshExpirationDate();
				lastUpdate = new Date().getTime();
			}
		}
		return expirationDate;
	}
}
