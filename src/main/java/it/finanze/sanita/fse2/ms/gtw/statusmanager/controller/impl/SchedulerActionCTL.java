/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import it.finanze.sanita.fse2.ms.gtw.statusmanager.controller.ISchedulerActionCTL;
import it.finanze.sanita.fse2.ms.gtw.statusmanager.scheduler.TxScheduler;

@RestController
public class SchedulerActionCTL extends AbstractCTL implements ISchedulerActionCTL{

	
	@Autowired
	private TxScheduler txScheduler;
	
	@Override
	public void runSchedulerAction() {
		txScheduler.action();
	}	
}
